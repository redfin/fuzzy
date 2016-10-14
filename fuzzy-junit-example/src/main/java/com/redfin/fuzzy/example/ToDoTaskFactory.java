package com.redfin.fuzzy.example;

import java.time.LocalDate;

public class ToDoTaskFactory {

	static ToDoTask create(String description, int dueInDays) {
		ToDoTask task = new ToDoTask();
		task.setDescription(description);
		task.setDue(LocalDate.now().plusDays(dueInDays));

		return task;
	}

}
