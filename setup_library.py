from motor.motor_asyncio import AsyncIOMotorClient
import asyncio
from datetime import datetime, timedelta

# Sample book data with valid ISBNs and publication years
books_data = [
    {
        "title": "The Great Gatsby",
        "author": "F. Scott Fitzgerald",
        "isbn": "978-0743273565",
        "publish_year": 1925,
        "description": "A story of the fabulously wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan.",
        "status": "UNAVAILABLE"
    },
    {
        "title": "To Kill a Mockingbird",
        "author": "Harper Lee",
        "isbn": "978-0446310789",
        "publish_year": 1960,
        "description": "The story of racial injustice and the loss of innocence in the American South.",
        "status": "UNAVAILABLE"
    },
    {
        "title": "1984",
        "author": "George Orwell",
        "isbn": "978-0451524935",
        "publish_year": 1949,
        "description": "A dystopian social science fiction novel and cautionary tale.",
        "status": "UNAVAILABLE"
    },
    {
        "title": "Pride and Prejudice",
        "author": "Jane Austen",
        "isbn": "978-0141439518",
        "publish_year": 1813,
        "description": "A romantic novel of manners about prejudice and marriage in Georgian England.",
        "status": "UNAVAILABLE"
    },
    {
        "title": "The Catcher in the Rye",
        "author": "J.D. Salinger",
        "isbn": "978-0316769488",
        "publish_year": 1951,
        "description": "The story of a teenage boy grappling with alienation in post-war America.",
        "status": "AVAILABLE"
    },
    {
        "title": "The Hobbit",
        "author": "J.R.R. Tolkien",
        "isbn": "978-0547928227",
        "publish_year": 1937,
        "description": "A fantasy novel about the adventures of Bilbo Baggins.",
        "status": "AVAILABLE"
    },
    {
        "title": "Brave New World",
        "author": "Aldous Huxley",
        "isbn": "978-0060850524",
        "publish_year": 1932,
        "description": "A dystopian novel envisioning a technologically advanced future society.",
        "status": "AVAILABLE"
    },
    {
        "title": "The Lord of the Rings",
        "author": "J.R.R. Tolkien",
        "isbn": "978-0618640157",
        "publish_year": 1954,
        "description": "An epic high-fantasy novel about the quest to destroy the One Ring.",
        "status": "AVAILABLE"
    },
    {
        "title": "Fahrenheit 451",
        "author": "Ray Bradbury",
        "isbn": "978-1451673319",
        "publish_year": 1953,
        "description": "A dystopian novel about a future American society where books are outlawed.",
        "status": "AVAILABLE"
    },
    {
        "title": "The Grapes of Wrath",
        "author": "John Steinbeck",
        "isbn": "978-0143039433",
        "publish_year": 1939,
        "description": "The story of a family's journey during the Great Depression.",
        "status": "AVAILABLE"
    },
    {
        "title": "One Hundred Years of Solitude",
        "author": "Gabriel García Márquez",
        "isbn": "978-0060883287",
        "publish_year": 1967,
        "description": "A landmark of magical realism and the history of the Buendía family.",
        "status": "AVAILABLE"
    },
    {
        "title": "The Odyssey",
        "author": "Homer",
        "isbn": "978-0140268867",
        "publish_year": 1614,
        "description": "Ancient Greek epic poem following Odysseus's journey home after the Trojan War.",
        "status": "AVAILABLE"
    },
    {
        "title": "Don Quixote",
        "author": "Miguel de Cervantes",
        "isbn": "978-0060934347",
        "publish_year": 1605,
        "description": "The story of an elderly man who loses his sanity and becomes a knight-errant.",
        "status": "AVAILABLE"
    },
    {
        "title": "War and Peace",
        "author": "Leo Tolstoy",
        "isbn": "978-0143039990",
        "publish_year": 1869,
        "description": "A narrative following five aristocratic families during the Napoleonic Era.",
        "status": "AVAILABLE"
    },
    {
        "title": "The Divine Comedy",
        "author": "Dante Alighieri",
        "isbn": "978-0142437223",
        "publish_year": 1320,
        "description": "An epic poem describing Dante's journey through Hell, Purgatory, and Paradise.",
        "status": "AVAILABLE"
    }
]


loans_data = [
    {
        "borrower_name": "John Doe",
        "book_name": "The Great Gatsby",
        "borrower_email": "john.doe@example.com",
        "borrow_date": datetime.utcnow(),
        "return_date": datetime.utcnow() + timedelta(days=30),
        "status": "ACTIVE",
    },
{
        "borrower_name": "Alpha",
        "book_name": "To Kill a Mockingbird",
        "borrower_email": "john.doe@example.com",
        "borrow_date": datetime.utcnow(),
        "return_date": datetime.utcnow() + timedelta(days=30),
        "status": "ACTIVE",
    },
{
        "borrower_name": "John Doe",
        "book_name": "1984",
        "borrower_email": "john.doe@example.com",
        "borrow_date": datetime.utcnow(),
        "return_date": datetime.utcnow() + timedelta(days=30),
        "status": "ACTIVE",
    },
{
        "borrower_name": "John Doe",
        "book_name": "Pride and Prejudice",
        "borrower_email": "john.doe@example.com",
        "borrow_date": datetime.utcnow(),
        "return_date": datetime.utcnow() + timedelta(days=30),
        "status": "ACTIVE",
    },
]


async def populate_database():
    # Connect to MongoDB
    client = AsyncIOMotorClient("mongodb+srv://jakub0472:up8Va8ivldgBClHV@cluster0.lmbuy.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0")
    db = client.library

    # Clear existing books and loans
    await db.books.delete_many({})
    await db.loans.delete_many({})

    # Insert books
    inserted_books = await db.books.insert_many(books_data)
    book_ids = [str(book_id) for book_id in inserted_books.inserted_ids]  # Extract book IDs

    # Assign book IDs to loans (assuming each loan corresponds to a book in books_data)
    for i, loan in enumerate(loans_data):
        loan["book_id"] = book_ids[i]

    # Insert loans with book IDs
    result_loan = await db.loans.insert_many(loans_data)

    print(f"Successfully inserted {len(result_loan.inserted_ids)} loans")

    # Print first few books to verify
    async for book in db.books.find().limit(3):
        print("\nSample book:")
        print(f"Title: {book['title']}")
        print(f"ISBN: {book['isbn']}")
        print(f"Publish Year: {book['publish_year']}")


if __name__ == "__main__":
    asyncio.run(populate_database())
