package com.example.vitaly.test;


public class EventOnDate {
    private long id;
    private String date;
    private Event event;
    private int numberOfTimes;

    public EventOnDate() {
    }

    public EventOnDate(String date, Event event, int number_of_times) {
        this.date = date;
        this.event = event;
        this.numberOfTimes = numberOfTimes;
    }

    public EventOnDate(long id, String date, Event event, int number_of_times) {
        this.id = id;
        this.date = date;
        this.event = event;
        this.numberOfTimes = numberOfTimes;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return this.date;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public void setNumberOfTimes(int numberOfTimes) {
        this.numberOfTimes = numberOfTimes;
    }

    public int getNumberOfTimes() {
        return this.numberOfTimes;
    }
}
