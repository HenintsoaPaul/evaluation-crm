package site.easy.to.build.crm.google.model.calendar;

import lombok.Data;

@Data
public class EventDateTime {
    private String date;
    private String dateTime;
    private String timeZone;
}