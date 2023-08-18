package com.example.planify;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private static final String COLUMN_IS_IN_UPDATE = "task_is_in_update";

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
    private static final String COLUMN_SUBTASK_POSITION ="position";

    // Deleted Tasks Table
    private static final String TABLE_DELETED_TASKS = "deleted_tasks";
    private static final String COLUMN_DELETED_TASK_ID = "_id";
    private static final String COLUMN_DELETED_TASK_NAME = "task_name";

    // Non schedulable hrs table
    private static final String TABLE_NON_SCHEDULABLE_HOURS = "non_schedulable_hours";
    private static final String COLUMN_NON_SCHEDULABLE_START_TIME = "start_time";
    private static final String COLUMN_NON_SCHEDULABLE_END_TIME = "end_time";
    public static final String COLUMN_NON_SCHEDULABLE_DAY = "day";

    private SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d yyyy HH:mm a", Locale.getDefault());

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
                    COLUMN_IS_DELETED + " INTEGER," +
                    COLUMN_IS_IN_UPDATE + " INTEGER" +
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
                    COLUMN_SUBTASK_POSITION + " INTEGER," +
                    "FOREIGN KEY(" + COLUMN_PARENT_TASK_ID + ") REFERENCES " +
                    TABLE_TASKS + "(" + COLUMN_ID + ")" +
                    ");";

    private static final String CREATE_TABLE_NON_SCHEDULABLE_HOURS = "CREATE TABLE " + TABLE_NON_SCHEDULABLE_HOURS + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_NON_SCHEDULABLE_START_TIME + " INTEGER, " +
            COLUMN_NON_SCHEDULABLE_END_TIME + " INTEGER, " +
            COLUMN_NON_SCHEDULABLE_DAY + " INTEGER);";



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
        db.execSQL(CREATE_TABLE_NON_SCHEDULABLE_HOURS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETED_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NON_SCHEDULABLE_HOURS);
        onCreate(db);
    }

    // CRUD operations methods...

    // Insert methods for each table...
    public long insertTask(Task task) {
        // Check for time conflicts
        if (hasTimeConflict(task)) {
            // Handle time conflict here (return -1 or throw an exception)
            return -1;
        } else if (hasNonSchedulableHourConflict(task.getTaskStartTime(), task.getTaskEndTime())){
            return -2;
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
        values.put(COLUMN_IS_IN_UPDATE, 0);

        long id = db.insert(TABLE_TASKS, null, values);
        Log.d("Planify", "Inserting subtasks: " + task.getSubTasks().size());

        for (SubTask subTask:task.getSubTasks()){
            subTask.setParentTaskId((int) id);
            if (subTask.getId() == 0){
                Log.d("Planify", "Inserting subtask " + subTask);
                this.insertSubTask(subTask);
            } else {
                this.updateSubTask(subTask);
                Log.d("Planify", "Updating subtask " + subTask);
            }
        }
        db.close();
        return id;
    }
    public int insertNonSchedulableHour(long startTime, long endTime, int day) {
        if (hasNonSchedulableHourConflict(startTime, endTime))
            return -1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NON_SCHEDULABLE_START_TIME, startTime);
        values.put(COLUMN_NON_SCHEDULABLE_END_TIME, endTime);
        values.put(COLUMN_NON_SCHEDULABLE_DAY, day);

        int row = (int) db.insert(TABLE_NON_SCHEDULABLE_HOURS, null, values);
        db.close();
        return row;
    }
    private boolean hasTimeConflict(Task newTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 AND " +
                COLUMN_IS_IN_UPDATE + " = 0 AND " +
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
        values.put(COLUMN_SUBTASK_POSITION, subTask.getPosition());

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
            Log.d("Planify", "has time conflict");
            return -1;
        } else if (hasNonSchedulableHourConflict(updatedTask.getTaskStartTime(), updatedTask.getTaskEndTime())){
            return -2;
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
        values.put(COLUMN_IS_IN_UPDATE, updatedTask.isInUpdate() ? 1 : 0);

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
                + COLUMN_ID + " <> ? AND " + COLUMN_IS_IN_UPDATE + " = 0 " +
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
        values.put(COLUMN_SUBTASK_TIME_REQUIRED, subTask.getTimeRequired());
        values.put(COLUMN_SUBTASK_POSITION, subTask.getPosition());

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

    // TODO Retrieve methods for each table...
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
            task.setInUpdate(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_IN_UPDATE)) == 1);
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
                task.setInUpdate(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_IN_UPDATE)) == 1);
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
                task.setInUpdate(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_IN_UPDATE)) == 1);
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
                    int position = cursor.getInt(cursor.getColumnIndex(COLUMN_SUBTASK_POSITION));
                    SubTask subTask = new SubTask(subTaskName, subTaskCompleted, taskId, timeRequired);
                    subTask.setPosition(position);
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
    public ArrayList<NonScheduleTime> getNonSchedulableHours() {
        ArrayList<NonScheduleTime> nonSchedulableHoursList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {
                COLUMN_NON_SCHEDULABLE_START_TIME,
                COLUMN_NON_SCHEDULABLE_END_TIME,
                COLUMN_NON_SCHEDULABLE_DAY
        };

        Cursor cursor = db.query(
                TABLE_NON_SCHEDULABLE_HOURS,
                columns,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long startTime = cursor.getLong(cursor.getColumnIndex(COLUMN_NON_SCHEDULABLE_START_TIME));
                long endTime = cursor.getLong(cursor.getColumnIndex(COLUMN_NON_SCHEDULABLE_END_TIME));
                int day = cursor.getInt(cursor.getColumnIndex(COLUMN_NON_SCHEDULABLE_DAY));
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_SUBTASK_ID));

                NonScheduleTime nonScheduleTime = new NonScheduleTime();
                nonScheduleTime.setId(id);
                nonScheduleTime.setStartTime(startTime);
                nonScheduleTime.setEndtime(endTime);
                nonScheduleTime.setDay(day);

                nonSchedulableHoursList.add(nonScheduleTime);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return nonSchedulableHoursList;
    }


    // Delete methods for each table...
    public void deleteSchedule(int scheduleId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(scheduleId) };

        db.delete(TABLE_SCHEDULE, selection, selectionArgs);
    }
    public void deleteSubtask(int subTaskID, int parentID) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(subTaskID) };

        db.delete(TABLE_SUBTASKS, selection, selectionArgs);
        Task taskById = this.getTaskById(parentID);
        if (taskById.getSubTasks().size() == 0){
            taskById.setDeleted(true);
            this.updateTask(taskById);
        }
    }
    public void deleteNonScheduleHr(int subTaskID) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(subTaskID) };

        db.delete(TABLE_NON_SCHEDULABLE_HOURS, selection, selectionArgs);
        db.close();
    }

    // Rescheduling
    public void rescheduleTask(Task taskToBeUpdated, Context context, int hrs, int days, int weeks) {
        // get time
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(new Date().getTime());
        c1.add(Calendar.HOUR, hrs);
        c1.add(Calendar.DAY_OF_MONTH, days);
        c1.add(Calendar.WEEK_OF_MONTH, weeks);

        // for start time
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);

        // Step 1: find next task with similar course name and find free time between
        long now = Math.max(c1.getTimeInMillis(), taskToBeUpdated.getTaskEndTime());
        Log.d("Planify", "time chosen: " + now);
        Task nextTask = this.findNextTask(taskToBeUpdated.getCourse(), now);
        Log.d("Planify", "Rescheduling task " + taskToBeUpdated);


        boolean isScheduled = false;
        ArrayList<Task> currentTasksToBeUpdated = new ArrayList<>();
        taskToBeUpdated.setDuration(taskToBeUpdated.getTaskEndTime() - taskToBeUpdated.getTaskStartTime());
        currentTasksToBeUpdated.add(taskToBeUpdated);

        long previousEndTime = now;
        while (currentTasksToBeUpdated.size() > 0 && nextTask != null){
            Log.d("Planify", "NextTask is not null " + nextTask);
            isScheduled = false;
            Task currentUpdateTask = currentTasksToBeUpdated.get(0);
            currentTasksToBeUpdated.remove(0);

            long timeRequired = currentUpdateTask.getDuration();
            List<FreeTime> freeTimes = this.findFreeTimes(previousEndTime, nextTask.getTaskEndTime());
            for (FreeTime freeTime:freeTimes){
                if (freeTime.getDuration() >= timeRequired){
                    Log.d("Planify", "Freetime " + freeTime);
                    Log.d("Planify", "Freetime exists before next task");
                    // Step 2: schedule new task start time
                    currentUpdateTask.setTaskStartTime(freeTime.getStartTime());
                    currentUpdateTask.setTaskEndTime(freeTime.getStartTime() + timeRequired);
                    previousEndTime = currentUpdateTask.getTaskEndTime();
                    // TODO CHECK HERE----------------------------------------------------------------
                    if (currentUpdateTask.getId() == 0)
                        this.insertTask(currentUpdateTask);
                    else{
                        this.updateTask(currentUpdateTask);
                        // cancel previous notifications
                        List<Schedule> schedules = this.getAllSchedulesForTask(currentUpdateTask.getId());
                        for (Schedule schedule:schedules){
                            NotificationUtils.cancelNotification(context, schedule.getNotificationId());
                            this.deleteSchedule(schedule.getId());
                        }
                    }
                    // schedule new notification
                    String message = "Start task: " + currentUpdateTask.getTaskName();
                    int notId = uniqueNotificationId();
                    NotificationUtils.scheduleNotification(context, message, (currentUpdateTask.getTaskStartTime() - (10 * 60 * 60)), notId);

                    message = "Upcoming task: " + currentUpdateTask.getTaskName();
                    NotificationUtils.scheduleNotification(context, message, currentUpdateTask.getTaskStartTime(), notId);

                    isScheduled = true;
                    break;
                }
            }
            if (isScheduled)
                continue;

            Log.d("Planify", "Next task to be edited: " + nextTask);
            // Step 3: replace with next task
            // 3a: check if `nextTask` duration is greater than or equal to `taskToBeUpdated` duration
            Log.d("Planify", "free time does not exists before next task");
            long nextTimeRequired = nextTask.getTaskEndTime() - nextTask.getTaskStartTime();
            if (nextTimeRequired >= timeRequired){
                Log.d("Planify", "Next task can be replaced with current task");
                // replace with next task
                currentUpdateTask.setTaskStartTime(nextTask.getTaskStartTime());
                currentUpdateTask.setTaskEndTime(nextTask.getTaskStartTime() + timeRequired);

                // set next task duration
                nextTask.setDuration(nextTask.getTaskEndTime() - nextTask.getTaskStartTime());
                // remove time conflict
                nextTask.setInUpdate(true);
                this.updateTask(nextTask);
                if (currentUpdateTask.getId() == 0)
                    this.insertTask(currentUpdateTask);
                else{
                    this.updateTask(currentUpdateTask);
                    // cancel previous notifications
                    List<Schedule> schedules = this.getAllSchedulesForTask(currentUpdateTask.getId());
                    for (Schedule schedule:schedules){
                        NotificationUtils.cancelNotification(context, schedule.getNotificationId());
                        this.deleteSchedule(schedule.getId());
                    }
                }

                // schedule new notification
                String message = "Start task: " + currentUpdateTask.getTaskName();
                int notId = uniqueNotificationId();
                NotificationUtils.scheduleNotification(context, message, (currentUpdateTask.getTaskStartTime() - (10 * 60 * 60)), notId);

                message = "Upcoming task: " + currentUpdateTask.getTaskName();
                NotificationUtils.scheduleNotification(context, message, currentUpdateTask.getTaskStartTime(), notId);

                Log.d("Planify", "Replaced available task with: " + currentUpdateTask);

                // update nextTask
                currentTasksToBeUpdated.add(0, nextTask);
                Log.d("Planify", "Next Task to be updated " + nextTask);

                // update previous endtime
                previousEndTime = currentUpdateTask.getTaskEndTime();
                nextTask = this.findNextTask(taskToBeUpdated.getCourse(), previousEndTime);
            } else {
                Log.d("Planify", "Next task cannot be replaced with current task, trying subtasks");
                Log.d("Planify", "Previous EndTime: " + sdf.format(previousEndTime));
                // swap subtasks with 1st subtasks that match time
                currentUpdateTask.sortSubTasks();

                // sort subtasks according to time
                List<SubTask> remainingSubtasks = currentUpdateTask.getSubTasks();
                List<SubTask> subTasksToBeAdded = new ArrayList<>();
                currentUpdateTask.setSubTasks(new ArrayList<>());

                long time1 = 0;
                while (true){
                    SubTask s = remainingSubtasks.get(0);
                    time1 += s.getTimeRequired() * 1000L;
                    if (nextTimeRequired >= time1){
                        subTasksToBeAdded.add(s);
                        remainingSubtasks.remove(0);
                    } else {
                        time1 -= s.getTimeRequired() * 1000L;
                        break;
                    }
                }

                for (SubTask s:subTasksToBeAdded)
                    Log.d("Planify", "ToBeAdded: " + s);

                long remainingSubTasksTime = 0;
                for (SubTask s:remainingSubtasks) {
                    Log.d("Planify", "ToBePushedForward: " + s);
                    remainingSubTasksTime += s.getTimeRequired() * 1000L;
                }

                // create new task
                Task newUpdateTask = currentUpdateTask.deepCopy();
                newUpdateTask.setSubTasks(remainingSubtasks);
                newUpdateTask.setDuration(remainingSubTasksTime);

                // update currentUpdateTask
                currentUpdateTask.setSubTasks(subTasksToBeAdded);
                currentUpdateTask.setTaskStartTime(previousEndTime);
                currentUpdateTask.setTaskEndTime(currentUpdateTask.getTaskStartTime() + time1);
                previousEndTime = currentUpdateTask.getTaskEndTime();
                if (currentUpdateTask.getSubTasks().size() == 0)
                    currentUpdateTask.setDeleted(true);

                Log.d("Planify", "Current task to replace: " + currentUpdateTask);

                Log.d("Planify", "Remaining tasks to be pushed forward: " + newUpdateTask);
                Log.d("Planify", "Previous EndTime: " + sdf.format(previousEndTime));

                // set next task duration
                nextTask.setDuration(nextTask.getTaskEndTime() - nextTask.getTaskStartTime());
                // remove time conflict
                nextTask.setInUpdate(true);
                this.updateTask(nextTask);
                if (currentUpdateTask.getId() == 0)
                    this.insertTask(currentUpdateTask);
                else {
                    this.updateTask(currentUpdateTask);
                    // cancel previous notifications
                    List<Schedule> schedules = this.getAllSchedulesForTask(currentUpdateTask.getId());
                    for (Schedule schedule:schedules){
                        NotificationUtils.cancelNotification(context, schedule.getNotificationId());
                        this.deleteSchedule(schedule.getId());
                    }
                }

                // schedule new notification
                String message = "Start task: " + currentUpdateTask.getTaskName();
                int notId = uniqueNotificationId();
                NotificationUtils.scheduleNotification(context, message, (currentUpdateTask.getTaskStartTime() - (10 * 60 * 60)), notId);

                message = "Upcoming task: " + currentUpdateTask.getTaskName();
                NotificationUtils.scheduleNotification(context, message, currentUpdateTask.getTaskStartTime(), notId);

                // reset time conflict
                nextTask.setInUpdate(false);
                Log.d("Planify", "Check for time conflict");
                Log.d("Planify", "NEXT TASK!!!!!!!!!\n " + nextTask);
                Log.d("Planify", "Previous EndTime: " + sdf.format(previousEndTime));

                // update nextTask
                currentTasksToBeUpdated.add(0, newUpdateTask);
                currentTasksToBeUpdated.add(1, nextTask);
                Log.d("Planify", "Previous EndTime: " + sdf.format(previousEndTime));
                nextTask = this.findNextTask(taskToBeUpdated.getCourse(), nextTask.getTaskEndTime());
            }
            // handle next task is null

        }

        if (currentTasksToBeUpdated.size() > 0){
            Log.d("Planify", "Next task is null hence loop will break");
            Log.d("Planify", "Previous EndTime: " + sdf.format(previousEndTime));

            int size = currentTasksToBeUpdated.size();
            for (int i=0; i<size; i++){
                Task task = currentTasksToBeUpdated.get(i);
                long timeRequired = task.getDuration();
                FreeTime nextFreeTime = this.findNextFreeTime(timeRequired, previousEndTime);
                task.setTaskStartTime(nextFreeTime.getStartTime());
                task.setTaskEndTime(nextFreeTime.getStartTime() + timeRequired);
                Log.d("Planify", "Updated task after null\n: " + task);
                currentTasksToBeUpdated.remove(0);
                if (task.getId() == 0)
                    this.insertTask(task);
                else{
                    this.updateTask(task);
                    // cancel previous notifications
                    List<Schedule> schedules = this.getAllSchedulesForTask(task.getId());
                    for (Schedule schedule:schedules){
                        NotificationUtils.cancelNotification(context, schedule.getNotificationId());
                        this.deleteSchedule(schedule.getId());
                    }
                }
                // schedule new notification
                String message = "Start task: " + task.getTaskName();
                int notId = uniqueNotificationId();
                NotificationUtils.scheduleNotification(context, message, (task.getTaskStartTime() - (10 * 60 * 60)), notId);

                message = "Upcoming task: " + task.getTaskName();
                NotificationUtils.scheduleNotification(context, message, task.getTaskStartTime(), notId);
                previousEndTime = task.getTaskEndTime();
                // create notification

            }

            // create notification


        }
    }
    public void rescheduleSubtask(SubTask subtask, Context context) {
        // Step 1: Find the Subtask's Parent Task and Course Name
//        SubTask subtask = databaseHelper.getSubtaskById(subtaskId);
        Task parentTask = this.getTaskById(subtask.getParentTaskId());
        String courseName = parentTask.getCourse();
        long parentEndTime = parentTask.getTaskEndTime();

        // Step 2: Find Next Record with Same Course Name and Later Start Time
        Task nextTask = this.findNextTask(courseName, parentEndTime);

        if (nextTask == null) {
            // Step 3: Reschedule the Subtask to the Next Available Time
            FreeTime freeTime = this.findNextFreeTime(subtask.getTimeRequired() * 1000, parentEndTime);

            Task task = new Task(courseName, parentTask.getTaskName(), freeTime.getStartTime(), freeTime.getEndTime(), false, 0.0, false);
            List<SubTask> subTasks = new ArrayList<>();
            subtask.setPosition(1);
            subTasks.add(subtask);
            task.setSubTasks(subTasks);
            this.insertTask(task);
            // delete previous subtask
//            this.deleteSubtask(subtask.getId(), parentTask.getId());
        } else {
            // Step 4: Check and Adjust Subtask Times
            SubTask currentSubTask = subtask;
            Task firstTask = parentTask;

            firstTask.removeSubTask(currentSubTask.getId());
            Log.d("Planify", "Current parentTask: " + firstTask);
            if (firstTask.getSubTasks().size() == 0){
                firstTask.setDeleted(true);
                // disable notifications
                List<Schedule> schedulesForTask = this.getAllSchedulesForTask(firstTask.getId());
                for (Schedule schedule:schedulesForTask)
                    NotificationUtils.cancelNotification(context, schedule.getId());
            }
            this.updateTask(firstTask);

            boolean isScheduled = false;
            Task previousTask = null;
            while (nextTask != null) {
                long cumulativeSubtaskTime = ((currentSubTask.getTimeRequired() + calculateCumulativeSubtaskTime(nextTask)) * 1000) + nextTask.getTaskStartTime();
                currentSubTask.setParentTaskId(nextTask.getId());
                currentSubTask.setPosition(1);
                List<SubTask> nextTaskSubTasks = nextTask.getSubTasks();
                Log.d("Planify", "Current Subtask: " + currentSubTask);
                int pos = 2;
                for (SubTask subTask:nextTaskSubTasks){
                    subTask.setPosition(pos);
                    pos++;
                }
                nextTaskSubTasks.add(currentSubTask);
                nextTask.setSubTasks(nextTaskSubTasks);

                // get free time immediately after current parent task
                FreeTime nextFreeTime = this.findNextFreeTime(currentSubTask.getTimeRequired()*1000, nextTask.getTaskEndTime());
                if (cumulativeSubtaskTime  <= nextTask.getTaskEndTime() || nextFreeTime.isImmediate()) {
                    Log.d("Planify", "Rescheduling Task: " + nextTask);
                    if (nextFreeTime.isImmediate())
                        nextTask.setTaskEndTime(nextTask.getTaskEndTime() + (currentSubTask.getTimeRequired() * 1000));
                    this.updateTask(nextTask);
                    isScheduled = true;
                    break;
                } else {
                    Log.d("Planify", "removing subtask from new task and updating: " + nextTask);
                    nextTask.sortSubTasks();
                    currentSubTask = nextTask.getSubTasks().get(nextTask.getSubTasks().size() - 1);
                    nextTask.getSubTasks().remove(nextTask.getSubTasks().size() - 1);
                    if (nextTask.getSubTasks().size() == 0){
                        nextTask.setDeleted(true);
                        // disable notifications
                        List<Schedule> schedulesForTask = this.getAllSchedulesForTask(nextTask.getId());
                        for (Schedule schedule:schedulesForTask)
                            NotificationUtils.cancelNotification(context, schedule.getId());
                    }
                    this.updateTask(nextTask);
                    Log.d("Planify", "Updated task: " + nextTask);
                }
                previousTask = nextTask;
                nextTask = this.findNextTask(courseName, nextTask.getTaskEndTime());
            }
            // Step 5: Handle case where suitable task not found
            if (!isScheduled){
                // Step 3: Reschedule the Subtask to the Next Available Time
                FreeTime freeTime = this.findNextFreeTime(currentSubTask.getTimeRequired() * 1000, previousTask.getTaskEndTime());

                Task task = new Task(courseName, parentTask.getTaskName(), freeTime.getStartTime(), freeTime.getEndTime(), false, 0.0, false);
                List<SubTask> subTasks = new ArrayList<>();
                subtask.setPosition(1);
                task.setSubTasks(subTasks);
                this.insertTask(task);
            }
        }
    }

    private long calculateCumulativeSubtaskTime(Task task) {
        long cumulativeTime = 0;
        for (SubTask subtask : task.getSubTasks()) {
            cumulativeTime += subtask.getTimeRequired();
        }
        return cumulativeTime;
    }
    @SuppressLint("Range")
    public Task findNextTask(String courseName, long parentEndTime) {
        SQLiteDatabase db = getReadableDatabase();

        // Select tasks with course name
        String query = "SELECT * " +
                " FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 AND "
                + COLUMN_IS_IN_UPDATE + " = 0 AND " +
                 COLUMN_TASK_START_TIME + " >= ? AND " + COLUMN_COURSE + " = ? ORDER BY " +
                 COLUMN_TASK_START_TIME + " LIMIT 1";

        String[] selectionArgs = {String.valueOf(parentEndTime), String.valueOf(courseName)};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        Task task = null;
        if (cursor != null && cursor.moveToFirst()) {
            task = new Task();
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
            cursor.close();
        }

        db.close();
        return task;
    }
    @SuppressLint("Range")
    public List<FreeTime> findFreeTimes(long startTime, long endTime) {
        List<FreeTime> freeTimes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        // Select tasks that overlap with the given range
        String query = "SELECT " + COLUMN_TASK_START_TIME + ", " + COLUMN_TASK_END_TIME +
                " FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 AND " + COLUMN_IS_IN_UPDATE + " = 0 AND " +
                "((" + COLUMN_TASK_START_TIME + " >= ? AND " + COLUMN_TASK_START_TIME + " <= ?) " +
                " OR (" + COLUMN_TASK_END_TIME + " >= ? AND " + COLUMN_TASK_END_TIME + " <= ?))";
        String[] selectionArgs = {String.valueOf(startTime), String.valueOf(endTime),
                String.valueOf(startTime), String.valueOf(endTime)};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        long previousEndTime = startTime;

        while (cursor.moveToNext()) {
            long taskStartTime = cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_START_TIME));
            long taskEndTime = cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_END_TIME));

            if (previousEndTime < taskStartTime && !hasNonSchedulableHourConflict(previousEndTime, taskStartTime)) {
                freeTimes.add(new FreeTime(previousEndTime, taskStartTime));
            }

            previousEndTime = taskEndTime;
        }

        if (previousEndTime < endTime && !hasNonSchedulableHourConflict(previousEndTime, endTime)) {
            freeTimes.add(new FreeTime(previousEndTime, endTime));
        }

        cursor.close();
        return freeTimes;
    }
    @SuppressLint("Range")
    public FreeTime findNextFreeTime(long timeRequired, long parentEndTime) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " +
                 TABLE_TASKS + " WHERE " + COLUMN_IS_DELETED + " = 0 AND " + COLUMN_IS_IN_UPDATE + " = 0 AND " +
                COLUMN_TASK_START_TIME + " >= ? OR " + COLUMN_TASK_END_TIME + " >= ? ";

        String[] selectionArgs = {String.valueOf(parentEndTime), String.valueOf(parentEndTime)};
        long previousEndTime = parentEndTime;

        Cursor cursor = db.rawQuery(query, selectionArgs);

        while (cursor.moveToNext()) {
            long taskStartTime = cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_START_TIME));
            long taskEndTime = cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_END_TIME));

            if (previousEndTime < taskStartTime && !hasNonSchedulableHourConflict(previousEndTime, taskStartTime) && ((taskStartTime - previousEndTime) * 1000) >= timeRequired) {
                // is free time
                return new FreeTime(previousEndTime, taskStartTime);
            }
            previousEndTime = taskEndTime;
        }

        // TODO test this !!!!!!!!!!!!!!!!!!!!!!!!!
        while (true){
            if (!hasNonSchedulableHourConflict(previousEndTime, (previousEndTime + timeRequired))){
                FreeTime freeTime = new FreeTime(previousEndTime, (previousEndTime + timeRequired));
                if (previousEndTime == parentEndTime)
                    freeTime.setImmediate(true);
                return freeTime;
            } else
                previousEndTime += timeRequired;
        }
    }
    private static int uniqueNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }
    public boolean hasNonSchedulableHourConflict(long startTime, long endTime) {

        boolean conflict = false;
        ArrayList<NonScheduleTime> nonSchedulableHours = this.getNonSchedulableHours();
        for (NonScheduleTime nonScheduleTime:nonSchedulableHours){

            // START HOUR
            Calendar c1 = Calendar.getInstance();
            c1.setTimeInMillis(nonScheduleTime.getStartTime());
            int startHour = c1.get(Calendar.HOUR_OF_DAY);
            int startMin = c1.get(Calendar.MINUTE);
            int startDay = c1.get(Calendar.DAY_OF_WEEK);

            c1.setTimeInMillis(startTime);
            c1.set(Calendar.HOUR_OF_DAY, startHour);
            c1.set(Calendar.MINUTE, startMin);
            c1.set(Calendar.SECOND, 0);
            c1.set(Calendar.MILLISECOND, 0);

            //END HOUR
            Calendar c2 = Calendar.getInstance();
            c2.setTimeInMillis(nonScheduleTime.getEndtime());
            int endHour = c2.get(Calendar.HOUR_OF_DAY);
            int endMin = c2.get(Calendar.MINUTE);
            int endDay = c2.get(Calendar.DAY_OF_WEEK);

            c2.setTimeInMillis(endTime);
            c2.set(Calendar.HOUR_OF_DAY, endHour);
            c2.set(Calendar.MINUTE, endMin);
            c2.set(Calendar.SECOND, 59);
            c2.set(Calendar.MILLISECOND, 999);

            // cehck start time
            Calendar c3 = Calendar.getInstance();
            c3.setTimeInMillis(startTime);
            int startDayOfWeek = c3.get(Calendar.DAY_OF_WEEK);

            if (startDayOfWeek >= startDay && startDayOfWeek <= endDay){
                if (c3.getTimeInMillis() >= c1.getTimeInMillis() && c3.getTimeInMillis() <= c2.getTimeInMillis())
                    conflict = true;

                c3.setTimeInMillis(endTime);
                if (c3.getTimeInMillis() >= c1.getTimeInMillis() && c3.getTimeInMillis() <= c2.getTimeInMillis())
                    conflict = true;
            }

        }
        return conflict;
    }


}



