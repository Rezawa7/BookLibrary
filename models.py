# models.py
from datetime import datetime, timedelta
from typing import Optional
from pydantic import BaseModel, Field, field_validator
from bson import ObjectId


class PyObjectId(ObjectId):
    @classmethod
    def __get_validators__(cls):
        yield cls.validate

    @classmethod
    def validate(cls, v):
        if not ObjectId.is_valid(v):
            raise ValueError("Invalid ObjectId")
        return ObjectId(str(v))

    @classmethod
    def __get_pydantic_json_schema__(cls, field_schema):
        field_schema.update(type="string")
        return field_schema


class MongoBaseModel(BaseModel):
    class Config:
        populate_by_name = True
        arbitrary_types_allowed = True
        json_encoders = {
            ObjectId: str
        }


class BookBase(MongoBaseModel):
    title: str = Field(..., min_length=1)
    author: str = Field(..., min_length=1)
    isbn: str = Field(..., min_length=10, max_length=17)
    publish_year: int = Field(..., ge=0, le=2026)
    description: str = Field(default="")
    status: str = Field(default="AVAILABLE")

    @field_validator('isbn')
    def validate_isbn(cls, v):
        # Remove hyphens and check if remaining characters are digits
        cleaned_isbn = v.replace('-', '')
        if not cleaned_isbn.isdigit():
            raise ValueError('ISBN must contain only digits and hyphens')

        # Check length after removing hyphens
        if len(cleaned_isbn) not in (10, 13):
            raise ValueError('ISBN must be 10 or 13 digits long (excluding hyphens)')

        return v


class Book(BookBase):
    id: str = Field(alias="_id")


def get_return_date():
    return datetime.now() + timedelta(days=30)


class LoanBase(MongoBaseModel):
    book_id: str
    book_name: str
    borrower_name: str
    borrower_email: str
    borrow_date: datetime
    return_date: datetime
    status: str = "ACTIVE"

class Loan(LoanBase):
    id: str = Field(alias="_id")
    book_id: ObjectId

class BookWithLoan(BaseModel):
    book: Book
    active_loan: Loan | None = None