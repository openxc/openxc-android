package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

public abstract class Command extends VehicleMessage implements KeyedMessage {

    private String mCommand;
        
    protected Command(String command) {
        setCommand(command);
    }

    protected Command(Map<String, Object> values) throws InvalidMessageFieldsException {
        super(values);
        if(!containsRequiredCommandField(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }
        setCommand(getValuesMap());
    }

    protected Command(String command, Map<String, Object> values) {
        super(values);
        setCommand(command);
    }
    
    public void setCommand(String command) {
        mCommand = command;
    }
    
    public void setCommand(Map<String, Object> values) {
        setCommand((String) values.remove(getCommandKey()));
    }

    public abstract String getCommandKey();

    public String getCommand() {
        return mCommand;
    }  

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(CommandRequest.COMMAND_KEY, getCommand());
        return new MessageKey(key);
    }
    
    protected Command() { }
    
    private boolean containsRequiredCommandField(Map<String, Object> map) {
        return map.containsKey(getCommandKey());
    }
    
}
