package com.example.vitaly.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import org.xml.sax.DTDHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.PendingIntent.getActivity;

public class EventsDBAdapter{

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context context;



    private static class DatabaseHelper extends SQLiteOpenHelper {
        //-------------------------------------------------------------------
        private static final String DATABASE_NAME = "events_database";
        private static final int DATABASE_VERSION = 3;

        // common column names:
            public static final String KEY_ID = "_id";

        // categories table:
            private static final String CATEGORIES_TABLE_NAME = "categories";

            public static final String KEY_CATEGORY_NAME = "name";

            private static final String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + CATEGORIES_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_CATEGORY_NAME + " TEXT);";

        // events table:
            private static final String EVENTS_TABLE_NAME = "events";

            public static final String KEY_CATEGORY_ID = "category_id";
            public static final String KEY_EVENT_NAME = "name";

            private static final String CREATE_EVENTS_TABLE = "CREATE TABLE " + EVENTS_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_CATEGORY_ID + " INTEGER, " +
                    KEY_EVENT_NAME + " TEXT);";

        // events_on_date table:
            private static final String EVENTS_ON_DATE_TABLE_NAME = "events_on_date";

            public static final String KEY_DATE = "date";
            public static final String KEY_EVENT_ID = "event_id";
            public static final String KEY_NUMBER_OF_TIMES = "number_of_times";

            private static final String CREATE_EVENTS_ON_DATE_TABLE = "CREATE TABLE " + EVENTS_ON_DATE_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATE + " TEXT, " +
                    KEY_EVENT_ID + " INTEGER, " +
                    KEY_NUMBER_OF_TIMES + " INTEGER);";
        //-------------------------------------------------------------------


        private Context helperContext;


        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.helperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_CATEGORIES_TABLE);
            db.execSQL(CREATE_EVENTS_TABLE);
            db.execSQL(CREATE_EVENTS_ON_DATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + EVENTS_ON_DATE_TABLE_NAME);

            onCreate(db);
        }
    }



    public EventsDBAdapter(Context context) {
        this.context = context;
    }

    public EventsDBAdapter open(){
        mDbHelper = new DatabaseHelper(context);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }



    // ------------------------ categories table methods ---------------- //

        public long insertCategory(String name)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.KEY_CATEGORY_NAME, name);

            long id = mDb.insert(DatabaseHelper.CATEGORIES_TABLE_NAME, null, contentValues);
            return id;
        }

        public Category retrieveCategory(long id)
        {
            String[] fieldsToSelect = {DatabaseHelper.KEY_CATEGORY_NAME};
            String[] argsToCompare = {String.valueOf(id)};

            Cursor cursor = mDb.query(DatabaseHelper.CATEGORIES_TABLE_NAME, fieldsToSelect,
                    DatabaseHelper.KEY_ID + "=?", argsToCompare, null, null, null, null);

            if (((cursor != null) && (cursor.getCount() > 0))) {
                cursor.moveToFirst();

                Category category = new Category();
                category.setId(id);
                category.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CATEGORY_NAME)));

                cursor.close();
                return category;
            }
            else
            {
                return null;
            }
        }

        public long retrieveCategoryId(Category category)
        {
            String[] fieldsToSelect = {DatabaseHelper.KEY_ID};
            String[] argsToCompare = {category.getName()};

            Cursor cursor = mDb.query(DatabaseHelper.CATEGORIES_TABLE_NAME, fieldsToSelect,
                    DatabaseHelper.KEY_CATEGORY_NAME + " =?", argsToCompare, null, null, null, null);

            long categoryId = -1;

            if ((cursor != null) && (cursor.getCount() > 0))
            {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(DatabaseHelper.KEY_ID);
                categoryId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID));
            }

            return categoryId;

        }

        public List<Category> retrieveAllCategories()
        {
            List<Category> categoriesList = new ArrayList<Category>();

            String[] fieldsToSelect = {DatabaseHelper.KEY_ID, DatabaseHelper.KEY_CATEGORY_NAME};

            Cursor cursor = mDb.query(DatabaseHelper.CATEGORIES_TABLE_NAME, fieldsToSelect,
                    null, null, null, null, null, null);

            if ((cursor != null) && (cursor.getCount() > 0))
            {
                cursor.moveToFirst();

                do {
                    Category category = new Category();

                    category.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                    category.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CATEGORY_NAME)));

                    categoriesList.add(category);

                } while (cursor.moveToNext());

                cursor.close();
            }

            return categoriesList;
        }

        public List<Pair<Category, Integer>> retrieveAllCategoriesSortedByPopularity()
        {
            List<Pair<Category, Integer>> sortedList = new ArrayList<Pair<Category, Integer>>();

            /*
                necessary SQL-query:
                SELECT a._id, a.name, b.count
                FROM categories AS a join (SELECT category_id, COUNT(category_id) AS count
                                        FROM events
                                        GROUP BY category_id) AS b
                ON a._id = b.category_id
                ORDER BY b.count DESC
            */

            Cursor cursor = mDb.rawQuery("SELECT a." + DatabaseHelper.KEY_ID + ", a." + DatabaseHelper.KEY_CATEGORY_NAME + ", b.count " +
                    "FROM " + DatabaseHelper.CATEGORIES_TABLE_NAME + " AS a " +
                    "JOIN " +
                    "(SELECT " + DatabaseHelper.KEY_CATEGORY_ID + ", COUNT(" + DatabaseHelper.KEY_CATEGORY_ID + ") AS count " +
                    "FROM " + DatabaseHelper.EVENTS_TABLE_NAME + " " +
                    "GROUP BY " + DatabaseHelper.KEY_CATEGORY_ID + ") As b " +
                    "ON a." + DatabaseHelper.KEY_ID + " = " + DatabaseHelper.KEY_CATEGORY_ID + " " +
                    "ORDER BY b.count DESC", null);

            if ((cursor != null) && (cursor.getCount() > 0))
            {
                cursor.moveToFirst();

                do {
                    Category category = new Category();

                    category.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                    category.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CATEGORY_NAME)));
                    int categoryPopularity = cursor.getInt(cursor.getColumnIndex("count"));

                    sortedList.add(new Pair<Category, Integer>(category, categoryPopularity));

                } while (cursor.moveToNext());

                cursor.close();
            }

            return sortedList;
        }

    // ------------------------ events table methods ---------------- //

        public long insertEvent(long categoryId, String name)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.KEY_CATEGORY_ID, categoryId);
            contentValues.put(DatabaseHelper.KEY_EVENT_NAME, name);

            long id = mDb.insert(DatabaseHelper.EVENTS_TABLE_NAME, null, contentValues);
            return id;
        }

        public Event retrieveEvent(long id)
        {
            String[] fieldsToSelect = {DatabaseHelper.KEY_CATEGORY_ID, DatabaseHelper.KEY_EVENT_NAME};
            String[] argsToCompare = {String.valueOf(id)};

            Cursor cursor = mDb.query(DatabaseHelper.EVENTS_TABLE_NAME, fieldsToSelect,
                    DatabaseHelper.KEY_ID + "=?", argsToCompare, null, null, null, null);

            if (((cursor != null) && (cursor.getCount() > 0))) {
                cursor.moveToFirst();

                long categoryId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_CATEGORY_ID));
                Category eventCategory = this.retrieveCategory(categoryId);

                Event event = new Event();
                event.setId(id);
                event.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_EVENT_NAME)));
                event.setCategory(eventCategory);

                cursor.close();
                return event;
            }
            else
            {
                return null;
            }

        }

        public long retrieveEventId(Event event)
        {
            String[] fieldsToSelect = {DatabaseHelper.KEY_ID};
            String[] argsToCompare = {event.getName(), String.valueOf(event.getCategory().getId())};

            Cursor cursor = mDb.query(DatabaseHelper.EVENTS_TABLE_NAME, fieldsToSelect,
                    DatabaseHelper.KEY_EVENT_NAME + "=? and " +
                    DatabaseHelper.KEY_CATEGORY_ID + "=?",
                    argsToCompare, null, null, null, null);

            long eventId = -1;

            if (((cursor != null) && (cursor.getCount() > 0)))
            {
                cursor.moveToFirst();

                eventId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_ID));
            }

            return eventId;
        }

        public List<Event> retrieveEventsRelatedToCategory(long categoryId)
        {
            List<Event> eventsList = new ArrayList<Event>();

            String[] fieldsToSelect = {DatabaseHelper.KEY_ID, DatabaseHelper.KEY_EVENT_NAME};
            String[] argsToCompare = {String.valueOf(categoryId)};

            Cursor cursor = mDb.query(DatabaseHelper.EVENTS_TABLE_NAME, fieldsToSelect,
                    DatabaseHelper.KEY_CATEGORY_ID + "=?",
                    argsToCompare, null, null, null, null);

            if ((cursor != null) && (cursor.getCount() > 0))
            {
                cursor.moveToFirst();

                Category commonCategory = this.retrieveCategory(categoryId);

                do {
                    Event event = new Event();

                    event.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                    event.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_EVENT_NAME)));
                    event.setCategory(commonCategory);

                    eventsList.add(event);

                } while (cursor.moveToNext());

                cursor.close();
            }

            return eventsList;

        }

        public List<Pair<Event, Integer>> retrieveEventsRelatedToCategorySortedByPopularity(long categoryId)
        {
            List<Pair<Event, Integer>> sortedList = new ArrayList<Pair<Event, Integer>>();

                /*
                    necessary SQL-query:
                    SELECT a._id, a.name, b.count

                    FROM
                        (
                            (SELECT _id, name
                            FROM events
                            WHERE category_id = categoryId) AS a

                            join

                            (SELECT event_id, COUNT(event_id) AS count
                            FROM events_on_date
                            GROUP BY event_id) AS b

                            ON a._id = b.event_id
                        )

                    ORDER BY b.count DESC
                */

            String[] argsToCompare = {String.valueOf(categoryId)};

            // TODO: rewrite sql statement using constant strings:
            Cursor cursor = mDb.rawQuery("SELECT a._id, a.name, b.count " +
                    "FROM " +
                    "(" +
                        "(SELECT _id, name " +
                        "FROM events " +
                        "WHERE category_id = ?) AS a " +

                        "JOIN " +

                        "(SELECT event_id, COUNT(event_id) AS count " +
                        "FROM events_on_date " +
                        "GROUP BY event_id) AS b " +

                        "ON a._id = b.event_id " +
                    ")" +
                    "ORDER BY b.count DESC", argsToCompare);

            if ((cursor != null) && (cursor.getCount() > 0))
            {
                cursor.moveToFirst();

                Category commonCategory = this.retrieveCategory(categoryId);

                do {
                    Event event = new Event();

                    // TODO: column indices
                    event.setId(cursor.getLong(0));
                    event.setName(cursor.getString(1));
                    event.setCategory(commonCategory);

                    int eventPopularity = cursor.getInt(2);

                    sortedList.add(new Pair<Event, Integer>(event, eventPopularity));

                } while (cursor.moveToNext());

                cursor.close();
            }

            return sortedList;
        }

    // ------------------------ events_on_date table methods ---------------- //

        public long insertEventOnDate(String date, long eventId, int numberOfTimes)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.KEY_DATE, date);
            contentValues.put(DatabaseHelper.KEY_EVENT_ID, eventId);
            contentValues.put(DatabaseHelper.KEY_NUMBER_OF_TIMES, numberOfTimes);

            long id = mDb.insert(DatabaseHelper.EVENTS_ON_DATE_TABLE_NAME, null, contentValues);
            return id;
        }

        public EventOnDate retrieveEventOnDate(long id)
        {
            String[] fieldsToSelect = {DatabaseHelper.KEY_DATE, DatabaseHelper.KEY_EVENT_ID, DatabaseHelper.KEY_NUMBER_OF_TIMES};
            String[] argsToCompare = {String.valueOf(id)};

            Cursor cursor = mDb.query(DatabaseHelper.EVENTS_ON_DATE_TABLE_NAME, fieldsToSelect,
                    DatabaseHelper.KEY_ID + "=?", argsToCompare, null, null, null, null);

            if (((cursor != null) && (cursor.getCount() > 0))) {
                cursor.moveToFirst();

                long eventId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_EVENT_ID));
                Event event = this.retrieveEvent(eventId);

                EventOnDate eventOnDate = new EventOnDate();
                eventOnDate.setId(id);
                eventOnDate.setDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_DATE)));
                eventOnDate.setEvent(event);
                eventOnDate.setNumberOfTimes(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_NUMBER_OF_TIMES)));

                cursor.close();
                return eventOnDate;
            }
            else
            {
                return null;
            }

        }

        public List<Pair<String, Integer>> retrieveSortedDatesToNumberOfEventsMap()
        {
            List<Pair<String, Integer>> eventsOnDateList = new ArrayList<Pair<String, Integer>>();

            String[] fieldsToSelect = {DatabaseHelper.KEY_DATE, "COUNT("+DatabaseHelper.KEY_DATE+")"};
            String fieldToGroupBy = DatabaseHelper.KEY_DATE;
            String fieldToOrderBy = DatabaseHelper.KEY_DATE + " DESC";

            Cursor cursor = mDb.query(DatabaseHelper.EVENTS_ON_DATE_TABLE_NAME, fieldsToSelect,
                    null, null, fieldToGroupBy, null, fieldToOrderBy, null);

            if ((cursor != null) && (cursor.getCount() > 0))
            {
                cursor.moveToFirst();

                do {
                    String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_DATE));
                    int numberOfEvents = cursor.getInt(cursor.getColumnIndex("COUNT(" + DatabaseHelper.KEY_DATE + ")"));

                    eventsOnDateList.add(new Pair(date, numberOfEvents));
                } while (cursor.moveToNext());

                cursor.close();
            }

            return eventsOnDateList;
        }

        public List<EventOnDate> retrieveEventsOnParticularDate(String date)
        {
            List<EventOnDate> eventsOnThisDateList = new ArrayList<EventOnDate>();

            String[] fieldsToSelect = {DatabaseHelper.KEY_ID, DatabaseHelper.KEY_EVENT_ID, DatabaseHelper.KEY_NUMBER_OF_TIMES};
            String whereStatement = DatabaseHelper.KEY_DATE + "=?";
            String[] argsToCompare = {date};

            Cursor cursor = mDb.query(DatabaseHelper.EVENTS_ON_DATE_TABLE_NAME, fieldsToSelect,
                    whereStatement,
                    argsToCompare, null, null, null, null);

            if ((cursor != null) && (cursor.getCount() > 0))
            {
                cursor.moveToFirst();

                do {
                    EventOnDate eventOnThisDate = new EventOnDate();

                    eventOnThisDate.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                    eventOnThisDate.setDate(date);

                    long eventId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_EVENT_ID));
                    Event event = this.retrieveEvent(eventId);
                    eventOnThisDate.setEvent(event);

                    eventOnThisDate.setNumberOfTimes(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_NUMBER_OF_TIMES)));

                    eventsOnThisDateList.add(eventOnThisDate);

                } while (cursor.moveToNext());

                cursor.close();
            }

            return eventsOnThisDateList;
        }
}
