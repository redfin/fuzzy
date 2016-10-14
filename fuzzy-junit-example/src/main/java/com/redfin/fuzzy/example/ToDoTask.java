package com.redfin.fuzzy.example;

import java.time.LocalDate;

public class ToDoTask {
	private String description;
	private LocalDate due;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getDue() {
		return due;
	}

	public void setDue(LocalDate due) {
		this.due = due;
	}
}
