package com.mycompany.mvpnews.Observer;


public interface ISubject {
    public void registerObserver(IObserver o);
    public void removerObserver(IObserver o);
    public void notifyObservers();
}
