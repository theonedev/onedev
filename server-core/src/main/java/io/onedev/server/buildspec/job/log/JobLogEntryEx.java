package io.onedev.server.buildspec.job.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import io.onedev.server.job.log.StyleBuilder;

public class JobLogEntryEx implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Date date;
	
	private final List<Message> messages;
	
	public JobLogEntryEx(Date date, List<Message> messages) {
		this.date = date;
		this.messages = messages;
	}
	
	public JobLogEntryEx(JobLogEntry entry) {
		this(entry.getDate(), Lists.newArrayList(new Message(new StyleBuilder().build(), entry.getMessage())));
	}
	
	// Handle ANSI escape codes according to https://en.wikipedia.org/wiki/ANSI_escape_code
	public static JobLogEntryEx parse(String text, StyleBuilder styleBuilder) {
		text = text.replace("\r\n", "\n");
		
		AtomicInteger cursor = new AtomicInteger(0);
		List<Message> messages = new ArrayList<>();
		
        Function<String, Void> textAppender = new Function<String, Void>() {

			@Override
			public Void apply(String text) {
                if (cursor.get() < 0)
                	cursor.set(0);
                List<Message> copy = new ArrayList<>(messages);
                messages.clear();
                int index = 0;
                for (Message message: copy) {
                    String currentText = message.getText();
                    if (cursor.get() >= index + currentText.length()) {
                    	messages.add(message);
                    } else {
                        if (cursor.get() > index) {
                            String beforeText = currentText.substring(0, cursor.get()-index);
                            messages.add(new Message(message.getStyle(), beforeText));
                        }
                        if (cursor.get() >= index)
                        	messages.add(new Message(styleBuilder.build(), text));
                        if (cursor.get() + text.length() < index) {
                            messages.add(message);
                        } else if (cursor.get() + text.length() < index + currentText.length()) {
                            String afterText = currentText.substring(cursor.get()+text.length()-index, currentText.length());
                            messages.add(new Message(message.getStyle(), afterText));
                        }
                    }
                    index += currentText.length();
                }

                if (cursor.get() >= index) {
                    messages.add(new Message(styleBuilder.build(), text));
                    cursor.set(index + text.length());
                } else {
                    cursor.addAndGet(text.length());
                }
                
                return null;
			}
			
		};
		
		BiFunction<Character, String, Void> actionPerformer = new BiFunction<Character, String, Void>() {

			private void eraseToEndOfLine() {
                if (cursor.get() < 0)
                	cursor.set(0);
                
                int index = 0;
                List<Message> copy = new ArrayList<>(messages);
                messages.clear();
                for (Message message: copy) {
                    String currentText = message.getText();
                    if (cursor.get() < index + currentText.length()) {
                        if (cursor.get() > index)
                        	messages.add(new Message(message.getStyle(), currentText.substring(0, cursor.get()-index)));
                    } else {
                    	messages.add(message);
                    }
                    index += currentText.length();
                }
			}
			
			private void eraseToCursor() {
                if (cursor.get() < 0)
                	cursor.set(0);
                
                int index = 0;
                List<Message> copy = new ArrayList<>(messages);
                messages.clear();
                for (Message message: copy) {
                    String currentText = message.getText();
                    if (cursor.get() <= index) {
                    	messages.add(message);
                    } else if (cursor.get() < index + currentText.length()) {
                    	String remainingText = currentText.substring(cursor.get()-index, currentText.length());
                    	messages.add(new Message(message.getStyle(), remainingText));
                    }
                    index += currentText.length();
                }
			}
			
			@Override
			public Void apply(Character action, String actionData) {
                switch (action) {
                case 'C':
                	cursor.addAndGet(Integer.parseInt(actionData));
                    break;
                case 'D':
                	cursor.addAndGet(-1*Integer.parseInt(actionData));
                    break;
                case 'K':
                case 'J':
                	if (actionData.length() == 0 || actionData.equals("0"))
                		eraseToEndOfLine();
                	else if (actionData.equals("1"))
                		eraseToCursor();
                	else
                		messages.clear();
                    break;
                case 'G':
                	if (actionData.length() == 0)
                		cursor.set(0);
                	else
                		cursor.set(Integer.parseInt(actionData)-1);
                	break;
                case 'H':
                case 'f':
                	List<String> fields = Splitter.on(";").splitToList(actionData); 
                	if (fields.size()  == 2)
                		cursor.set(Integer.parseInt(fields.get(1))-1);
                	else
                		cursor.set(0);
                	break;
                case 'm':
                	fields = Splitter.on(";").splitToList(actionData); 
                	for (int i=0; i<fields.size(); i++) {
                		String field = fields.get(i);
                        if (field.equals("0") || field.equals("")) {
                        	styleBuilder.reset();
                        } else if (field.equals("1")) {
	                        styleBuilder.setBold(true);
                        } else if (field.equals("22")) {
	                        styleBuilder.setBold(false);
                        } else if (field.equals("7") || field.equals("27")) {
                        	styleBuilder.swapForegroundAndBackGround();
                        } else if (field.equals("30") || field.equals("31") || field.equals("32") 
                        		|| field.equals("33") || field.equals("34") || field.equals("35")
                        		|| field.equals("36") || field.equals("37") || field.equals("90") 
                        		|| field.equals("91") || field.equals("92") || field.equals("93") 
                        		|| field.equals("94") || field.equals("95") || field.equals("96") 
                        		|| field.equals("97")) {
	                    	styleBuilder.setColor(field);
                        } else if (field.equals("40") || field.equals("41") || field.equals("42") 
                        		|| field.equals("43") || field.equals("44") || field.equals("45")
                        		|| field.equals("46") || field.equals("47") || field.equals("100") 
                        		|| field.equals("101") || field.equals("102") || field.equals("103") 
                        		|| field.equals("104") || field.equals("105") || field.equals("106") 
                        		|| field.equals("107")) {
	                    	styleBuilder.setBackgroundColor(field);
                        } else if (field.equals("39")) {
	                    	styleBuilder.setColor(Style.FOREGROUND_COLOR_DEFAULT);
                        } else if (field.equals("49")) {
	                    	styleBuilder.setColor(Style.BACKGROUND_COLOR_DEFAULT);
                        } else if (field.equals("38") || field.equals("48") || field.equals("58")) {
                            if (fields.get(++i).equals("5")) 
                            	i++;
                            else 
                            	i += 3;
                        }
                	}
		            break;
                }
				return null;
			};
		};
		
		StringBuilder actionDataBuilder = null;
        StringBuilder currentTextBuilder = new StringBuilder();

        int index = 0;
        while (index < text.length()) {
            char currentChar = text.charAt(index++);
            if (currentChar == '\u0008') {
                if (currentTextBuilder.length() != 0) {
                    textAppender.apply(currentTextBuilder.toString());
                    currentTextBuilder.setLength(0);
                }
                cursor.decrementAndGet();                
            } else if (currentChar == '\r') {
                if (currentTextBuilder.length() != 0) {
                    textAppender.apply(currentTextBuilder.toString());
                    currentTextBuilder.setLength(0);
                }
            	cursor.set(0);
            } else if (actionDataBuilder != null) {
                if (Character.isLetter(currentChar)) {
                    if (currentTextBuilder.length() != 0) {
                        textAppender.apply(currentTextBuilder.toString());
                        currentTextBuilder.setLength(0);
                    }
                    actionPerformer.apply(currentChar, actionDataBuilder.toString());
                    actionDataBuilder = null;
                } else {
                	actionDataBuilder.append(currentChar);
                }
            } else if (currentChar == '\u001b') {
            	if (index < text.length()) {
            		char nextChar = text.charAt(index);
            		if (nextChar == '[') {            			
            			actionDataBuilder = new StringBuilder();
                        index++;
            		} else if (nextChar == ']') {
            			index++;
            			nextChar = text.charAt(index);
            			if (nextChar == '0') {
            				while (text.charAt(++index) != '\u0007');
            			} else if (nextChar == '8') {
            				index += 2;
            			} else if (nextChar == 'P') {
            				index += 8;
            			}
            		} else if (nextChar >= '\u0040' && nextChar <= '\u005f' 
            				|| nextChar >= '\u0060' && nextChar <= '\u007e'
            				|| nextChar >= '\u0030' && nextChar <= '\u003f') {
            			index++;
            		} else if (nextChar >= '\u0020' && nextChar <= '\u002f') {
            			index++;
            			while (true) {
            				nextChar = text.charAt(index++);
            				if (nextChar >= '\u0030' && nextChar <= '\u007e')
            					break;
            			}
            		}
            	}
            } else if (currentChar != '\u0007' && currentChar != '\u000c') {
                currentTextBuilder.append(currentChar);
            }
        }
        
        if (currentTextBuilder.length() != 0)
        	textAppender.apply(currentTextBuilder.toString());
        
        return new JobLogEntryEx(new Date(), new ArrayList<>(messages));
	}

	public Date getDate() {
		return date;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public String getMessageText() {
		StringBuilder builder = new StringBuilder();
		for (Message message: messages)
			builder.append(message.getText());
		return builder.toString();
	}
	
	@Nullable
	public JobLogEntry getSpaceEfficientVersion() {
		StringBuilder builder = new StringBuilder();
		for (Message message: messages) {
			if (message.getStyle().isDefault())
				builder.append(message.getText());
			else
				return null;
		}
		return new JobLogEntry(date, builder.toString());
	}
	
}
