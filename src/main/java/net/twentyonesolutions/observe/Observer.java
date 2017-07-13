package net.twentyonesolutions.observe;

import java.util.Date;

/**
 * Created by Igal on 3/6/2017.
 */
public interface Observer {

	default void notify(Object... args) {

		StringBuilder sb = new StringBuilder(256);
		sb.append((new Date()).toString());
		sb.append(" ");

		for (Object arg : args) {
			sb.append(arg.toString());
			sb.append("; ");
		}

		System.out.println(sb.toString());
	}

}