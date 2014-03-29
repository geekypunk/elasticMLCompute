package com.cs5412.taskmanager;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class TaskDaoAdaptor implements JsonSerializer<TaskDao> {

 @Override
 public JsonElement serialize(TaskDao src, Type typeOfSrc,
            JsonSerializationContext context)
    {

        JsonObject obj = new JsonObject();
        obj.addProperty("userId", src.getUserId());
        obj.addProperty("taskName",src.getTaskName());
        obj.addProperty("taskDescription",src.getTaskDescription());
        obj.addProperty("taskType",src.getTaskType());
        obj.addProperty("reportUrl",src.getReportUrl());
        obj.addProperty("status",src.getStatus().name());
        obj.addProperty("isSeen",src.isSeen());
        obj.addProperty("taskId",src.getTaskId());
        obj.addProperty("parentTaskId",src.getParentTaskId());
        obj.addProperty("isSeen",src.isSeen());
        final JsonArray subTasks = new JsonArray();
        for (final Integer task : src.getAllSubTasks()) {
            final JsonPrimitive taskID = new JsonPrimitive(task);
            subTasks.add(taskID);
          }
        obj.add("subTasks", subTasks);


        return obj;
    }


}