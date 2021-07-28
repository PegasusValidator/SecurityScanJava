package de.pegasusvalidator;

import io.reactivex.rxjava3.subjects.BehaviorSubject;
import javafx.util.Pair;

public class IPhoneObserver {


    private static BehaviorSubject<Pair<Double, Boolean>> backupStatus = BehaviorSubject.create();

    public static BehaviorSubject<Pair<Double, Boolean>> getBackupStatusSubject() {
        return backupStatus;
    }

    public static void setBackupStatus(Double progress, boolean isFinished) {
        backupStatus.onNext(new Pair<>(progress, isFinished));
    }

    private static BehaviorSubject<Pair<String, Boolean>> iphoneStatus = BehaviorSubject.create();

    public static BehaviorSubject<Pair<String, Boolean>> getIphonestatusSubject() {
        return iphoneStatus;
    }

    public static void setIphoneStatus(String uuid, boolean isConnected) {
        iphoneStatus.onNext(new Pair<>(uuid, isConnected));
    }


}
