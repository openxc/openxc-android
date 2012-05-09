package com.openxc.sources;

public class DataSourceResourceException extends DataSourceException {
	public DataSourceResourceException(String message, Throwable cause) {
        super(message, cause);
    }

	public DataSourceResourceException(String message) {
        super(message);
    }
}
