package com.example.vitaly.test;


public class Event {
    private long id;
    private Category category;
    private String name;

    public Event() {
    }

    public Event(Category category, String name) {
        this.category = category;
        this.name = name;
    }

    public Event(long id, Category category, String name) {
        this.id = id;
        this.category = category;
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
