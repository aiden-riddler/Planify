package com.example.planify;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "learning_schedule.db";
    private static final int DATABASE_VERSION = 2;

    // Tasks Table
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_COURSE = "course";
    private static final String COLUMN_TASK_NAME = "task_name";
    private static final String COLUMN_DAY_OF_WEEK = "day_of_week";
    private static final String COLUMN_TASK_START_TIME = "task_start_time";
    private static final String COLUMN_TASK_END_TIME = "task_end_time";
    private static final String COLUMN_IS_COMPLETED = "is_completed";
    private static final String COLUMN_COMPLETION_PERCENTAGE = "completion_percentage";
    private static final String COLUMN_IS_DELETED = "task_is_deleted";

    // Schedule Table
    private static final String TABLE_SCHEDULE = "schedule";
    private static final String COLUMN_SCHEDULE_ID = "_id";
    private static final String COLUMN_TASK_ID = "task_id";  // Foreign key to tasks
    private static final String COLUMN_NOTIFICATION_ID = "notification_id";

    // Subtasks Table
    private static final String TABLE_SUBTASKS = "subtasks";
    private static final String COLUMN_SUBTASK_ID = "_id";
    private static final String COLUMN_SUBTASK_NAME = "subtask_name";
    private static final String COLUMN_SUBTASK_COMPLETED = "subtask_completed";
    private static final String COLUMN_PARENT_TASK_ID = "parent_task_id";  // Foreign key to tasks
    private static final String COLUMN_SUBTASK_TIME_REQUIRED = "time_required";

    // Deleted Tasks Table
    private static final String TABLE_DELETED_TASKS = "deleted_tasks";
    private static final String COLUMN_DELETED_TASK_ID = "_id";
    private static final String COLUMN_DELETED_TASK_NAME = "task_name";

    // create tables
    private static final String TABLE_CREATE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_COURSE + " TEXT, " +
                    COLUMN_TASK_NAME + " TEXT, " +
                    COLUMN_TASK_START_TIME + " REAL, " +
                    COLUMN_TASK_END_TIME + " REAL, " +
                    COLUMN_IS_COMPLETED + " INTEGER, " +
                    COLUMN_COMPLETION_PERCENTAGE + " REAL," +
                    COLUMN_IS_DELETED + " INTEGER" +
                    ");";

    private static final String TABLE_CREATE_SCHEDULE =
            "CREATE TABLE " + TABLE_SCHEDULE + " (" +
                    COLUMN_SCHEDULE_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_TASK_ID + " INTEGER, " +
                    COLUMN_NOTIFICATION_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_TASK_ID + ") REFERENCES " +
                    TABLE_TASKS + "(" + COLUMN_ID + ")" +
                    ");";

    private static final String TABLE_CREATE_SUBTASKS =
            "CREATE TABLE " + TABLE_SUBTASKS + " (" +
                    COLUMN_SUBTASK_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_SUBTASK_NAME + " TEXT, " +
                    COLUMN_SUBTASK_COMPLETED + " INTEGER, " +
                    COLUMN_PARENT_TASK_ID + " INTEGER, " +
                    COLUMN_SUBTASK_TIME_REQUIRED + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_PARENT_TASK_ID + ") REFERENCES " +
                    TABLE_TASKS + "(" + COLUMN_ID + ")" +
                    ");";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables here
        db.execSQL(TABLE_CREATE_TASKS);
        db.execSQL(TABLE_CREATE_SCHEDULE);
        db.execSQL(TABLE_CREATE_SUBTASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETED_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // CRUD operations methods...

    // Insert methods for each table...

    public long insertTask(Task task) {
        // Check for time conflicts
        if (hasTimeConflict(task)) {
            // Handle time conflict here (return -1 or throw an exception)
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE, task.getCourse());
        values.put(COLUMN_TASK_NAME, task.getTaskName());
        values.put(COLUMN_TASK_START_TIME, task.getTaskStartTime());
        values.put(COLUMN_TASK_END_TIME, task.getTaskEndTime());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_COMPLETION_PERCENTAGE, task.getCompletionPercentage());
        values.put(COLUMN_IS_DELETED, task.isDeleted() ? 1 : 0);

        long id = db.insert(TABLE_TASKS, null, values);
        Log.d("Planify", "Inserting subtasks: " + task.getSubTasks().size());

        for (SubTask subTask:task.getSubTasks()){
            subTask.setParentTaskId((int) id);
            this.insertSubTask(subTask);
        }
        db.close();
        return id;
    }
    private boolean hasTimeConflict(Task newTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 AND " +
                "((" + COLUMN_TASK_START_TIME + " >= ? AND " + COLUMN_TASK_START_TIME + " < ?) " +
                " OR (" + COLUMN_TASK_END_TIME + " > ? AND " + COLUMN_TASK_END_TIME + " <= ?) " +
                " OR (" + COLUMN_TASK_START_TIME + " <= ? AND " + COLUMN_TASK_END_TIME + " >= ?)) ";

        String[] selectionArgs = {
                String.valueOf(newTask.getTaskStartTime()),
                String.valueOf(newTask.getTaskEndTime()),
                String.valueOf(newTask.getTaskStartTime()),
                String.valueOf(newTask.getTaskEndTime()),
                String.valueOf(newTask.getTaskStartTime()),
                String.valueOf(newTask.getTaskEndTime())
        };

        Cursor cursor = db.rawQuery(selectQuery, selectionArgs);
        boolean hasConflict = cursor.moveToFirst();
        cursor.close();
        db.close();
        return hasConflict;
    }
    public long insertSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_ID, schedule.getTaskId());
        values.put(COLUMN_NOTIFICATION_ID, schedule.getNotificationId());

        long id = db.insert(TABLE_SCHEDULE, null, values);
        db.close();
        return id;
    }
    public long insertSubTask(SubTask subTask) {
        Log.d("Planify", "Inserting subtask: " + subTask.toString());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBTASK_NAME, subTask.getSubTaskName());
        values.put(COLUMN_SUBTASK_COMPLETED, subTask.isSubTaskCompleted() ? 1 : 0);
        values.put(COLUMN_PARENT_TASK_ID, subTask.getParentTaskId());
        values.put(COLUMN_SUBTASK_TIME_REQUIRED, subTask.getTimeRequired());

        long id = db.insert(TABLE_SUBTASKS, null, values);
        db.close();
        return id;
    }

    // Update methods for each table...
    public int updateTask(Task updatedTask) {

        // Retrieve the original task
        Task originalTask = getTaskById(updatedTask.getId());

        // Check for time conflicts with other tasks (excluding the original task)
        if (hasTimeConflict(updatedTask, originalTask)) {
            // Handle time conflict here (return -1 or throw an exception)
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE, updatedTask.getCourse());
        values.put(COLUMN_TASK_NAME, updatedTask.getTaskName());
        values.put(COLUMN_TASK_START_TIME, updatedTask.getTaskStartTime());
        values.put(COLUMN_TASK_END_TIME, updatedTask.getTaskEndTime());
        values.put(COLUMN_IS_COMPLETED, updatedTask.isCompleted() ? 1 : 0);
        values.put(COLUMN_COMPLETION_PERCENTAGE, updatedTask.getCompletionPercentage());
        values.put(COLUMN_IS_DELETED, updatedTask.isDeleted() ? 1 : 0);

        int rowsAffected = db.update(TABLE_TASKS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(updatedTask.getId())});

        for (SubTask subTask:updatedTask.getSubTasks()){
            subTask.setParentTaskId(updatedTask.getId());
            if (subTask.getId() == 0){
                this.insertSubTask(subTask);
            } else {
                this.updateSubTask(subTask);
            }
        }
        db.close();
        return rowsAffected;
    }
    private boolean hasTimeConflict(Task newTask, Task originalTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 AND "
                + COLUMN_ID + " <> ? " +
                " AND ((" +
                " (" + COLUMN_TASK_START_TIME + " >= ? AND " + COLUMN_TASK_START_TIME + " < ?) " +
                " OR (" + COLUMN_TASK_END_TIME + " > ? AND " + COLUMN_TASK_END_TIME + " <= ?) " +
                " OR (" + COLUMN_TASK_START_TIME + " <= ? AND " + COLUMN_TASK_END_TIME + " >= ?) " +
                "))";

        String[] selectionArgs = {
                String.valueOf(originalTask.getId()),
                String.valueOf(newTask.getTaskStartTime()),
                String.valueOf(newTask.getTaskEndTime()),
                String.valueOf(newTask.getTaskStartTime()),
                String.valueOf(newTask.getTaskEndTime()),
                String.valueOf(newTask.getTaskStartTime()),
                String.valueOf(newTask.getTaskEndTime())
        };

        Cursor cursor = db.rawQuery(selectQuery, selectionArgs);
        boolean hasConflict = cursor.moveToFirst();
        cursor.close();
        db.close();
        return hasConflict;
    }
    public int updateSubTask(SubTask subTask) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBTASK_NAME, subTask.getSubTaskName());
        values.put(COLUMN_SUBTASK_COMPLETED, subTask.isSubTaskCompleted() ? 1 : 0);
        values.put(COLUMN_PARENT_TASK_ID, subTask.getParentTaskId());

        int rowsAffected = db.update(TABLE_SUBTASKS, values, COLUMN_SUBTASK_ID + " = ?",
                new String[]{String.valueOf(subTask.getId())});

        if (rowsAffected > 0) {
            updateTaskCompletion(subTask.getParentTaskId());
        }

        db.close();
        return rowsAffected;
    }
    private void updateTaskCompletion(int taskId) {
        List<SubTask> subTaskList = getAllSubtasksForTask(taskId);

        int totalSubtasks = subTaskList.size();
        int completedSubtasks = 0;

        for (SubTask subTask : subTaskList) {
            if (subTask.isSubTaskCompleted()) {
                completedSubtasks++;
            }
        }

        double completionPercentage = (double) completedSubtasks / totalSubtasks * 100;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETION_PERCENTAGE, completionPercentage);

        if (completedSubtasks == totalSubtasks) {
            values.put(COLUMN_IS_COMPLETED, 1);
        }

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(taskId)});

        db.close();
    }

    // Retrieve methods for each table...
    @SuppressLint("Range")
    public Task getTaskById(int taskId) {
        String selectQuery = "SELECT * FROM " + TABLE_TASKS +
                " WHERE " + COLUMN_ID + " = ? AND " + COLUMN_IS_DELETED + " = 0 ORDER BY " + COLUMN_TASK_START_TIME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(taskId)});

        Task task = null;
        if (cursor != null && cursor.moveToFirst()) {
            task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            task.setCourse(cursor.getString(cursor.getColumnIndex(COLUMN_COURSE)));
            task.setTaskName(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_NAME)));
            task.setTaskStartTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_START_TIME)));
            task.setTaskEndTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_END_TIME)));
            task.setCompleted(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_COMPLETED)) == 1);
            task.setCompletionPercentage(cursor.getDouble(cursor.getColumnIndex(COLUMN_COMPLETION_PERCENTAGE)));
            task.setDeleted(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_DELETED)) == 1);
            task.setSubTasks(this.getAllSubtasksForTask(taskId));
            cursor.close();
        }

        db.close();
        return task;
    }
    @SuppressLint("Range")
    public String[] getCourses() {
        List<String> courses = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String course = cursor.getString(cursor.getColumnIndex(COLUMN_COURSE));
                if (!courses.contains(course))
                    courses.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return courses.toArray(new String[0]);
    }
    @SuppressLint("Range")
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 ORDER BY " + COLUMN_TASK_START_TIME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task();
                int taskId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                task.setId(taskId);
                task.setCourse(cursor.getString(cursor.getColumnIndex(COLUMN_COURSE)));
                task.setTaskName(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_NAME)));
                task.setTaskStartTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_START_TIME)));
                task.setTaskEndTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_END_TIME)));
                task.setCompleted(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_COMPLETED)) == 1);
                task.setCompletionPercentage(cursor.getDouble(cursor.getColumnIndex(COLUMN_COMPLETION_PERCENTAGE)));
                task.setDeleted(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_DELETED)) == 1);
                task.setSubTasks(this.getAllSubtasksForTask(taskId));
                taskList.add(task);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return taskList;
    }
    @SuppressLint("Range")
    public List<Task> getTasksOnDate(Date targetDate) {
        // get day difference
        // create calendar instance
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // get beginning of day
        Date beginningOfDay = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        // get end of day
        Date endOfDay = calendar.getTime();

        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Convert the target date to a long value
//        long targetTime = targetDate.getTime();

        // Query to get tasks with start and end times that intersect the target date
        String query = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 AND " +
                COLUMN_TASK_START_TIME + " >= ? AND " +
                COLUMN_TASK_START_TIME + " <= ? ORDER BY " + COLUMN_TASK_START_TIME;

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(beginningOfDay.getTime()), String.valueOf(endOfDay.getTime())});

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                int taskId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                task.setId(taskId);
                task.setCourse(cursor.getString(cursor.getColumnIndex(COLUMN_COURSE)));
                task.setTaskName(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_NAME)));
                task.setTaskStartTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_START_TIME)));
                task.setTaskEndTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_END_TIME)));
                task.setCompleted(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_COMPLETED)) == 1);
                task.setCompletionPercentage(cursor.getDouble(cursor.getColumnIndex(COLUMN_COMPLETION_PERCENTAGE)));
                task.setDeleted(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_DELETED)) == 1);
                task.setSubTasks(this.getAllSubtasksForTask(taskId));
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return tasks;
    }
    @SuppressLint("Range")
    public List<Schedule> getAllSchedulesForTask(int taskId) {
        List<Schedule> scheduleList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_SCHEDULE +
                " WHERE " + COLUMN_TASK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(taskId)};

        Cursor cursor = db.rawQuery(selectQuery, selectionArgs);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_SCHEDULE_ID));
                    int notificationId = cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATION_ID));

                    Schedule schedule = new Schedule(taskId, notificationId);
                    schedule.setId(id);

                    scheduleList.add(schedule);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return scheduleList;
    }
    @SuppressLint("Range")
    public List<SubTask> getAllSubtasksForTask(int taskId) {
        List<SubTask> subTaskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_SUBTASKS +
                " WHERE " + COLUMN_PARENT_TASK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(taskId)};

        Cursor cursor = db.rawQuery(selectQuery, selectionArgs);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_SUBTASK_ID));
                    String subTaskName = cursor.getString(cursor.getColumnIndex(COLUMN_SUBTASK_NAME));
                    boolean subTaskCompleted = cursor.getInt(cursor.getColumnIndex(COLUMN_SUBTASK_COMPLETED)) == 1;
                    int timeRequired = cursor.getInt(cursor.getColumnIndex(COLUMN_SUBTASK_TIME_REQUIRED));
                    SubTask subTask = new SubTask(subTaskName, subTaskCompleted, taskId, timeRequired);
                    subTask.setId(id);

                    subTaskList.add(subTask);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return subTaskList;
    }
    @SuppressLint("Range")
    public List<FreeTime> findFreeTimes(long startTime, long endTime) {
        List<FreeTime> freeTimes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        // Select tasks that overlap with the given range
        String query = "SELECT " + COLUMN_TASK_START_TIME + ", " + COLUMN_TASK_END_TIME +
                " FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 AND " +
                "((" + COLUMN_TASK_START_TIME + " >= ? AND " + COLUMN_TASK_START_TIME + " <= ?) " +
                " OR (" + COLUMN_TASK_END_TIME + " >= ? AND " + COLUMN_TASK_END_TIME + " <= ?))";
        String[] selectionArgs = {String.valueOf(startTime), String.valueOf(endTime),
                String.valueOf(startTime), String.valueOf(endTime)};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        long previousEndTime = startTime;

        while (cursor.moveToNext()) {
            long taskStartTime = cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_START_TIME));
            long taskEndTime = cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_END_TIME));

            if (previousEndTime < taskStartTime) {
                freeTimes.add(new FreeTime(previousEndTime, taskStartTime));
            }

            previousEndTime = taskEndTime;
        }

        if (previousEndTime < endTime) {
            freeTimes.add(new FreeTime(previousEndTime, endTime));
        }

        cursor.close();
        return freeTimes;
    }

    // Delete methods for each table...
    public void deleteSchedule(int scheduleId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(scheduleId) };

        db.delete(TABLE_SCHEDULE, selection, selectionArgs);
    }

    // Rescheduling

//    in my database helper class i want to write a method to reschedule a subtask, that means changing its parent task id. The method should first find the subtask's parent task, then find the coursename of the parent task, then retrieve the next record that has the smae coursename and their starttime is greater than the parent task's time. if this record does not exist create a new task in the next available free time (we had implemented a method to get free time).
}


