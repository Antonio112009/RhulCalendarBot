/*
 * Copyright (c)
 * Created and developed by Antonio112009
 */

package Entity;

import lombok.Data;

@Data
public class Event {

    private String[] time;
    private String courseCode;
    private String subject;
    private String type;
    private String lecturer;
    private String location;

}
