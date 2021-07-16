package com.kamtar.transport.api.sockets.classes;

public class Message {
	 
    private Integer nb_notifications;
    private String text;
    
    
	public Integer getNb_notifications() {
		return nb_notifications;
	}
	public void setNb_notifications(Integer nb_notifications) {
		this.nb_notifications = nb_notifications;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Message() {
		super();
	}
	public Message(Integer nb_notifications, String text) {
		super();
		this.nb_notifications = nb_notifications;
		this.text = text;
	}
 
    
}
