package net.twentyonesolutions.observe;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Igal on 3/6/2017.
 */
public class Observable {

	Object observedObject = null;
	ConcurrentHashMap<Observer, Boolean> observers = new ConcurrentHashMap<>();

	public Observable(Object observedObject) {

		if (observedObject != null)
			this.observedObject = observedObject;
		else
			this.observedObject = this;
	}

	public void addObserver(Observer observer) {

		observers.put(observer, true);
	}

	public void removeObserver(Observer observer) {

		observers.remove(observer);
	}

	public void removeAll() {

		observers.clear();
	}

	public void notify(Object... args) {

		for (Observer observer : observers.keySet()) {

			observer.notify(observedObject, args);
		}
	}

}