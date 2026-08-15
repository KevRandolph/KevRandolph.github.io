"""
***********************************************************************************
* Kevin Randolph
* DAT 340: Client/Server Development
* Professor Tad Kellogg
* Assignment 5-1 Poject One
* 2/08/26
* 
* This CRUD Python module defines a reusable class that connects to the AAC 
* MongoDB database & provides full create, read, update and delete functionality. 
* The module allows new records to be added, existing records to be queried using 
* search criteria, records to be updated, and records to be removed as needed. Query 
* results are returned as lists so they can be easily reused by other scripts or 
* applications. Database credentials are passed in when the class is instantiated 
* rather than hard coded in the file, which improves security and keeps sensitive 
* information out of the source code. This design makes the module easy to reuse, 
* maintain, and extend for future phases of the project.
************************************************************************************
"""

from pymongo import MongoClient
from pymongo.errors import PyMongoError


class AnimalShelter(object):
    """CRUD operations for Animal collection in MongoDB"""

    def __init__(self, username, password, host="localhost", port=27017,
                 db_name="aac", collection_name="animals", auth_db="admin"):
        """
        Initialize MongoDB connection.
        """
        self.client = None
        self.database = None
        self.collection = None

        try:
            uri = f"mongodb://{username}:{password}@{host}:{port}/?authSource={auth_db}"
            self.client = MongoClient(uri)
            self.database = self.client[db_name]
            self.collection = self.database[collection_name]

            # Quick connection check (forces auth/connection)
            self.client.admin.command("ping")

        except PyMongoError as e:
            print(f"MongoDB connection error: {e}")

    def create(self, data):
        """
        Insert a document into the collection.
        data: dictionary of key/value pairs acceptable to insert_one()

        Returns:
            True if insert succeeded, else False
        """
        if data is None or not isinstance(data, dict) or len(data) == 0:
            return False

        if self.collection is None:
            return False

        try:
            result = self.collection.insert_one(data)
            return result.acknowledged is True
        except PyMongoError as e:
            print(f"Insert error: {e}")
            return False

    def read(self, query):
        """
        Query for documents in the collection using find().

        query: dictionary of key/value lookup pairs acceptable to find()

        Returns:
            list of results if successful, else []
        """
        if query is None or not isinstance(query, dict):
            return []

        if self.collection is None:
            return []

        try:
            cursor = self.collection.find(query)
            return list(cursor)
        except PyMongoError as e:
            print(f"Read error: {e}")
            return []
        
    def update(self, query, update_data, many=False):
        """
        Update document(s) in the collection.

        Args:
            query (dict): key/value lookup pairs acceptable to find()
            update_data (dict): update operation dict acceptable to update_one/update_many
                                Example: {"$set": {"outcome_type": "Transfer"}}
            many (bool): if True, uses update_many; otherwise update_one

        Returns:
            int: number of documents modified
        """
        if query is None or not isinstance(query, dict) or len(query) == 0:
            return 0

        if update_data is None or not isinstance(update_data, dict) or len(update_data) == 0:
            return 0

        if self.collection is None:
            return 0

        try:
            if many:
                result = self.collection.update_many(query, update_data)
            else:
                result = self.collection.update_one(query, update_data)

            return int(result.modified_count)
        except PyMongoError as e:
            print(f"Update error: {e}")
            return 0

    def delete(self, query, many=False):
        """
        Delete document(s) from the collection.

        Args:
            query (dict): key/value lookup pairs acceptable to find()
            many (bool): if True, uses delete_many; otherwise delete_one

        Returns:
            int: number of documents deleted
        """
        if query is None or not isinstance(query, dict) or len(query) == 0:
            return 0

        if self.collection is None:
            return 0

        try:
            if many:
                result = self.collection.delete_many(query)
            else:
                result = self.collection.delete_one(query)

            return int(result.deleted_count)
        except PyMongoError as e:
            print(f"Delete error: {e}")
            return 0

