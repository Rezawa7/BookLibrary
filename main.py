from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from motor.motor_asyncio import AsyncIOMotorClient
from typing import List
import uvicorn
from bson import ObjectId
from datetime import datetime

from models import Book, Loan, BookWithLoan, BookBase, LoanBase

app = FastAPI()

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# MongoDB connection
client = AsyncIOMotorClient("mongodb+srv://jakub0472:up8Va8ivldgBClHV@cluster0.lmbuy.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0")
db = client.library
db.books.create_index([("title", "text"), ("author", "text"), ("description", "text")])


async def serialize_book(book_doc):
    return Book(
        _id=str(book_doc["_id"]),
        title=book_doc.get("title", ""),
        author=book_doc.get("author", ""),
        isbn=book_doc.get("isbn", "N/A"),
        publish_year=book_doc.get("publish_year", 0),
        description=book_doc.get("description", ""),
        status=book_doc.get("status", "AVAILABLE")
    )


async def serialize_loan(loan_doc):
    if loan_doc is None:
        return None
    return Loan(
        _id=str(loan_doc["_id"]),  # Convert _id to string for serialization
        book_id=ObjectId(loan_doc["book_id"]),  # Convert book_id to string for serialization
        book_name=loan_doc.get("book_name", ""),
        borrower_name=loan_doc.get("borrower_name", ""),
        borrower_email=loan_doc.get("borrower_email", ""),
        borrow_date=loan_doc.get("borrow_date", ""),
        return_date=loan_doc.get("return_date", ""),
        status=loan_doc.get("status", "")
    )


@app.get("/api/books", response_model=List[Book])
async def get_all_books():
    books = await db.books.find().to_list(1000)
    return [await serialize_book(book) for book in books]


@app.get("/api/books/search", response_model=List[Book])
async def search_books(query: str):
    books = await db.books.find({"$text": {"$search": query}}).to_list(1000)
    return [await serialize_book(book) for book in books]


@app.get("/api/books/{book_id}", response_model=BookWithLoan)
async def get_book(book_id: str):
    book = await db.books.find_one({"_id": ObjectId(book_id)})
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")
    active_loan = await db.loans.find_one({
        "book_id": book_id,
    })
    book = await serialize_book(book)

    active_loan = await serialize_loan(active_loan)

    data = {"book": book, "active_loan": active_loan}

    return BookWithLoan(**data)


@app.post("/api/books", response_model=Book)
async def create_book(book: BookBase):
    book_dict = book.model_dump()
    result = await db.books.insert_one(book_dict)
    created_book = await db.books.find_one({"_id": result.inserted_id})
    return await serialize_book(created_book)


@app.put("/api/books/{book_id}", response_model=Book)
async def update_book(book_id: str, book: BookBase):
    book_dict = book.model_dump()
    result = await db.books.update_one(
        {"_id": ObjectId(book_id)},
        {"$set": book_dict}
    )
    if result.modified_count == 0:
        raise HTTPException(status_code=404, detail="Book not found")
    updated_book = await db.books.find_one({"_id": ObjectId(book_id)})
    return await serialize_book(updated_book)


@app.delete("/api/books/{book_id}")
async def delete_book(book_id: str):
    result = await db.books.delete_one({"_id": ObjectId(book_id)})
    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Book not found")
    await db.loans.delete_many({"book_id": ObjectId(book_id)})
    return {"message": "Book deleted"}


@app.post("/api/loans", response_model=Loan)
async def create_loan(loan: LoanBase):
    # Check if book exists and is available
    book = await db.books.find_one({"_id": ObjectId(str(loan.book_id))})
    print(book)
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")

    if book["status"] != "AVAILABLE":
        raise HTTPException(status_code=400, detail="Book is not available for loan")

    # Check if user has reached loan limit
    existing_loans = await db.loans.find({
        "borrower_email": loan.borrower_email,
        "status": "ACTIVE"
    }).to_list(1000)

    if len(existing_loans) >= 3:
        raise HTTPException(
            status_code=400,
            detail="Borrower has reached maximum number of allowed loans (3)"
        )

    # Create the loan
    loan_dict = loan.model_dump()
    loan_dict["book_id"] = str(loan_dict["book_id"])
    # Convert datetime objects to strings
    loan_dict["borrow_date"] = loan_dict["borrow_date"].isoformat()
    loan_dict["return_date"] = loan_dict["return_date"].isoformat()
    loan_dict["status"] = "ACTIVE"

    result = await db.loans.insert_one(loan_dict)

    # Update book status
    await db.books.update_one(
        {"_id": ObjectId(str(loan.book_id))},
        {"$set": {"status": "UNAVAILABLE"}}
    )

    created_loan = await db.loans.find_one({"_id": result.inserted_id})
    print(created_loan)
    return await serialize_loan(created_loan)


@app.get("/api/loans", response_model=List[Loan])
async def get_all_loans():
    loans = await db.loans.find().to_list(1000)
    results = []
    for loan in loans:
        serialized_loan = await serialize_loan(loan)
        results.append(serialized_loan)

    return results


@app.get("/api/loans/active", response_model=List[BookWithLoan])
async def get_active_loans():
    active_loans_cursor = db.loans.find({"status": "ACTIVE"})
    active_loans = await active_loans_cursor.to_list(1000)

    results = []
    for loan in active_loans:
        book = await db.books.find_one({"_id": ObjectId(loan["book_id"])})
        if book:
            serialized_book = await serialize_book(book)
            serialized_loan = await serialize_loan(loan)
            results.append(BookWithLoan(book=serialized_book, active_loan=serialized_loan))

    return results


@app.put("/api/loans/{loan_id}/return")
async def return_book(loan_id: str):
    # Fetch the loan record from the database
    loan = await db.loans.find_one({"_id": ObjectId(loan_id)})
    if not loan:
        raise HTTPException(status_code=404, detail="Loan not found")

    # Ensure that the book ID is in ObjectId format
    book_id = loan.get("book_id")
    if not book_id:
        raise HTTPException(status_code=404, detail="Book ID not found in loan record")
    if isinstance(book_id, str):
        book_id = ObjectId(book_id)

    # Update the loan's status to "RETURNED"
    await db.loans.update_one(
        {"_id": ObjectId(loan_id)},
        {
            "$set": {
                "status": "RETURNED",
                "return_date": datetime.now(),
                "book_id": "AAAAAAAAAAAAAAAAAAAAAAAA"
            }
        }
    )

    # Update the book's status to "AVAILABLE"
    result = await db.books.update_one(
        {"_id": book_id},
        {"$set": {"status": "AVAILABLE"}}
    )
    if result.matched_count == 0:
        raise HTTPException(status_code=404, detail="Book not found")

    return {"message": "Book returned successfully"}


if __name__ == "__main__":
    uvicorn.run("main:app", reload=True)
