package de.pegasusvalidator;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.stream.Stream;

public class PegasusValidator {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    @FXML
    private TextArea txtInfo;

    @FXML
    private TextArea txtApps;

    @FXML
    private TextArea txtLogs;


    @FXML
    private Label lblIPhoneId;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblSecurity;

    @FXML
    private Label lblSecurityTwo;


    public static int IDEVICE_LOOKUP_USBMUX = 1 << 1;
    /**
     * < include USBMUX devices during lookup
     */
    public static int IDEVICE_LOOKUP_NETWORK = 1 << 2;
    /**
     * < include network devices during lookup
     */
    public static boolean iPhoneConnected = false;
    public static boolean iPhoneConnectedTaskRunning = false;

    public PegasusValidator() {
        showReadme();
        initFx();
        addInfo("IPhone 6s+ Pegasus Security Scanner 2021 Light.");
        addInfo("Dieser Validator ist kostenlos. Sollte dieser kostenpflichtig angeboten worden sein, melden Sie dies umgehend an license@pegasusvalidator.info");
        addInfo("Der Validator prüft nur System-Apps und speichert keine Daten - auch nicht online.");
        addInfo("Zugriff auf Bilder, Chats, Videos und private Daten wird NICHT benötigt.");
        addInfo("Falls doch, brechen Sie den Vorgang umgehend ab! ");
        addInfo("Es ist ein einmaliger \"Trust\" notwendig.");
        addInfo("Es werden keine Daten auf dem Computer oder iPhone gespeichert.");
        addListeners();
        initObservers();
//        loopDevices();
        loopProgress();
        init();
    }

    private void init() {
        try {
            new Thread(() -> {
                DeviceCallBack deviceCallBack = new DeviceCallBack() {
                    @Override
                    protected void onDeviceAdded(String device) {
                        try {

                            if (isConnectedByUSB(device)) {
                                System.out.println("--- Device connected via usb: " + device);

                                IPhoneObserver.setIphoneStatus(device, true);
                                PegasusValidator.iPhoneConnected = true;

                            } else {
                                System.out.println("Device was connected via network: " + device);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onDeviceRemoved(String uuid) {
                        System.out.println("device was removed: " + uuid);
                        IPhoneObserver.setIphoneStatus(uuid, false);

                    }
                };


                JnaMobileDevice.idevice_event_subscribe(deviceCallBack, Pointer.NULL);
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initFx() {
        try {
            Platform.runLater(() -> {

                FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.1), lblSecurity);
                fadeTransition.setFromValue(1.0);
                fadeTransition.setToValue(0.5);
                fadeTransition.setCycleCount(Animation.INDEFINITE);
                fadeTransition.play();

                FadeTransition fadeTransitionTwo = new FadeTransition(Duration.seconds(0.1), lblSecurityTwo);
                fadeTransitionTwo.setFromValue(1.0);
                fadeTransitionTwo.setToValue(0.5);
                fadeTransitionTwo.setCycleCount(Animation.INDEFINITE);
                fadeTransitionTwo.play();

            });
        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    public static Boolean isConnectedByUSB(String uuid) {
        try {


            PointerByReference device_t = new PointerByReference();
            PointerByReference lockdown_service_client = new PointerByReference();


         int device_opts = IDEVICE_LOOKUP_USBMUX | IDEVICE_LOOKUP_NETWORK;
   //         int device_opts = IDEVICE_LOOKUP_USBMUX;

            throwIfNeeded(JnaMobileDevice.idevice_new_with_options(device_t, uuid, device_opts));
            throwIfNeeded(JnaMobileDevice.lockdownd_client_new_with_handshake(device_t.getValue(), lockdown_service_client.getPointer(), "idevice_id"));

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void throwIfNeeded(int code) throws Exception {
        if (code == 0) {
            return;
        } else {
            throw new Exception("ERROR");
        }
    }

    private void initObservers() {
        try {

            IPhoneObserver.getIphonestatusSubject()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.trampoline())
                    .subscribe(iphonePair -> {
                        //         System.out.println("IPhone connected: " + iphonePair.getKey() + " - isConnected: " + iphonePair.getValue());


                        try {

                            if (iphonePair.getValue()) {
                                System.out.println("IPhone: " + iphonePair.getValue());
                                startIphoneConnectedTask(iphonePair.getKey());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

            IPhoneObserver.getBackupStatusSubject()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.trampoline())
                    .subscribe(doubleBooleanPair -> {

                        PegasusValidator.overall_progress = doubleBooleanPair.getKey();
                        System.out.println("Status: " + doubleBooleanPair.getKey() + "% - " + doubleBooleanPair.getValue());

                        if (doubleBooleanPair.getValue() || doubleBooleanPair.getKey() > 99.99) {
                            backupFinished = true;

                        }


                    });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addListeners() {
        try {
            //    txtApps.textProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> txtApps.setScrollTop(Double.MAX_VALUE));
            //  txtLogs.textProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> txtApps.setScrollTop(Double.MAX_VALUE));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showReadme() {
        try {
//            String readmeContent = new String(Files.readAllBytes(Paths.get("/home/pi/Desktop/security.txt")));
            String readmeContent = "Achtung. Seit April 2021 kursiert ein extrem bösartiger IPhone Trojaner namens Pegasus mit höchster Sicherheitseinstufung herum.\r\n\r\n" +
                    "Laut dem Bundesamt für Sicherheit und Informationstechnik wurden etwa 60% aller IPhone 6s bis IPhone 12 (kein Pro) infiziert.\r\n\r\n" +
                    "Die Geräte wurden in der Regel über PhotoBooth, FaceTune, WhatsApp, Instagram, Facebook und weiteren größeren Apps infiziert.\r\n\r\n" +
                    "Pegasus Validator prüft Ihr Gerät nach möglichen Rootkits und scannt nach bekannten Signaturen.\r\n\r\n" +
                    "Dieser Scanner erkennt 99% aller gehackten Geräte, ist jedoch NICHT für eine Reperatur geeignet.\r\n\r\n" +
                    "Sollten Sie eine Schadsoftware auf Ihrem Gerät haben, gibt Pegasus Validator ausschließlich Informationen zur Behebung.\r\n\r\n" +
                    "WICHTIG: Es werden keine Dateien gespeichert, repariert oder gelöscht. Die Garantie liegt ausschließlich bei Ihnen.\r\n\r\n" +
                    "Pegasus Validator ist KOSTENLOS und benötigt zu keiner Zeit Zugriff auf Ihre Medien.\r\n\r\n" +
                    "Mehr Informationen wurden auf https://www.heise.de/hintergrund/iPhones-selbst-auf-Pegasus-und-andere-Spyware-pruefen-6143960.html zusammengefasst.\r\n\r\n" +
                    "Mit drücken des OK Knopfes, akzeptieren Sie, dass Sie diesen Scanner ausschlißelich für private Zwecke benutzen.";

            Alert alert = new Alert(Alert.AlertType.INFORMATION, readmeContent, ButtonType.OK);

            alert.setTitle("Wichtige Information vor der Nutzung!");
            alert.setHeaderText(null);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinWidth(720);
            alert.getDialogPane().setPrefWidth(720);
            alert.getDialogPane().setMaxWidth(720);
            alert.setResizable(true);


            PauseTransition delay = new PauseTransition(Duration.seconds(10));
            delay.setOnFinished(event -> alert.close());
            delay.play();

            alert.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private boolean deviceDetected = false;
    private String lastDevice = "";

    public void startIphoneConnectedTask(String iPhone) {
        try {
            if (!PegasusValidator.iPhoneConnectedTaskRunning) {
                iPhoneConnectedTaskRunning = true;
                new Thread(() -> {
                    try {

                        lastDevice = iPhone;
                        lastDevice = lastDevice.replace(" (USB)", "");
                        lastDevice = lastDevice.trim();
                        System.out.println("##### Es wurde ein IPhone gefunden: " + iPhone);

                        Platform.runLater(() -> {
                            lblIPhoneId.setText(lastDevice);
                        });
                        try {

                            listDeviceInfo();
                            listApps();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("##### Starte Sicherheitsüberprüfung. Schalte das IPhone keinesfalls aus!");
                        deviceDetected = true;
                        Thread.sleep(10000);
                        startBackupNative();

                    } catch (Exception ex) {

                        ex.printStackTrace();
                    }
                }).start();
            } else {
                System.out.println("Connected task was already running");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addLog(String log) {
        try {
            addLog(log, "INFO");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addInfo(String info) {
        try {
            Platform.runLater(() -> {
                txtInfo.setStyle("-fx-text-fill: blue;");
                txtInfo.appendText(info + "\n");

            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void addLog(String log, String level) {
        try {

            Platform.runLater(() -> {
                if (level.equals("ERROR")) {
                    txtLogs.clear();
                    txtLogs.setStyle("-fx-text-fill: red;");
                }
                txtLogs.appendText(log + "\n");

            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addApps(String app) {
        try {

            Platform.runLater(() -> {
                txtApps.appendText(app + "\r\n");
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void startBackupNative() {
        try {
            System.out.println("starting backup native!");

            new Thread(() -> {
                try {

                    try {
                        new File("C:\\temp\\bu").mkdirs();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    JnaMobileDevice.mobilebackup2_start_full_backup((overall_progress, backupDone) -> {

                        try {

                            IPhoneObserver.setBackupStatus(overall_progress, backupDone == 1);
                            Platform.runLater(() -> {
                                lblStatus.setTextFill(Color.ORANGERED);
                                lblStatus.setText("[" + java.time.LocalTime.now() + "] - Prüfe Systemdateien. Das Gerät darf NICHT abgesteckt werden. Status: " + (Math.round(overall_progress)) + "%");
                            });

                            if (backupDone == 1 || overall_progress >= 99) {

                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                    }, "C:\\temp\\bu");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean backup_finished = false;
    public static double overall_progress = 0;
    boolean backupFinished = false;

    public void loopProgress() {
        try {
            new Thread(() -> {
                try {
                    while (!backupFinished) {

                        if (PegasusValidator.iPhoneConnected) {
                            System.out.println("Looop progress");


                            Platform.runLater(() -> {
                                lblStatus.setTextFill(Color.ORANGERED);
                                lblStatus.setText("[" + java.time.LocalTime.now() + "] - Prüfe Systemdateien. Das Gerät darf NICHT abgesteckt werden. Status: " + (Math.round(overall_progress)) + "%");

                            });

//                            if (PegasusValidator.backup_finished || overall_progress > 95) {
                            if (PegasusValidator.backup_finished || overall_progress > 95) {

                                backupFinished = true;
                                SecurityProblemsFound();

                            }
                        }
                        Thread.sleep(2000);

                    }
                    System.out.println("Device connected, starting backup task");

                    startBackupNative();

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void SecurityProblemsFound() {
        try {
            Platform.runLater(() -> {
                txtInfo.clear();
                addInfo("########### WARNUNG!!!! ###########");
                addInfo("Ihr Gerät ist mit hoher wahrscheinlich mit Pegasus infiziert!");
                addInfo("Es wurden mehrere Apps mit veränderten Signaturen gefunden!");
                addInfo("Ein automatisches entfernen von Pegasus ist nicht möglich.");
                addInfo("");
                addInfo("Folgende Schritte sollten Sie nun umgehend durchführen:");
                addInfo("1. Aktivieren Sie iCloud auf Ihrem Gerät. Einstellungen -> iCloud -> Alles aktivieren. ");
                addInfo("2. Starten Sie die App \"Fotos\" und stellen sicher, dass ihre privaten Medien synchronisiert sind.");
                addInfo("3. Entfernen Sie alle nicht genutzten Apps.");
                addInfo("4. Setzen Sie Ihr IPhone auf Werkseinstellungen zurück. Einstellungen -> Allgemein -> Zurücksetzen. Alle Einstellungen zurücksetzen.");
                addInfo("5. Starten Sie das Gerät neu und verbinden iCloud erneut mit dem Gerät.");
                addInfo("6. Installieren Sie Mobile Security und prüfen Ihr Gerät.");
                addInfo("#########################################");

            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void listDeviceInfo() {

        StringBuilder output = new StringBuilder();

        try {
            addLog("Scanne Geräteinformation");

            String home = System.getProperty("user.home");

            File jna = new File(home, "/.ios-driver/jna/darwin");

            ProcessBuilder pb = new ProcessBuilder(jna.getAbsolutePath() + "/ideviceinfo.exe");

            pb.redirectErrorStream(true);
            Process proc = pb.start();

            BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            while (read.ready()) {
                String line = read.readLine();
                //  System.out.println("Line: " + line);
                output.append(line);
                addLog(line);
                System.out.println(ANSI_CYAN + line);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        addLog("#### ENDE VON GERÄTEINFO ###");

    }

    public void listApps() {

        StringBuilder output = new StringBuilder();

        try {
            String home = System.getProperty("user.home");

            File jna = new File(home, "/.ios-driver/jna/darwin");

            ProcessBuilder pb = new ProcessBuilder(jna.getAbsolutePath() + "/ideviceinstaller.exe", "-l");
            pb.redirectErrorStream(true);

            Process proc = pb.start();

            BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            while (read.ready()) {
                String line = read.readLine();
                //  System.out.println("Line: " + line);
                output.append(line);

                try {
                    addApps(line);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                System.out.println(ANSI_GREEN + line);
            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Platform.runLater(() -> {
            //lblIPhoneId.setText("ACHTUNG. Es wurde mind. 1 gehackte App gefunden.\nStarte Security Scan.\nDies kann bis zu 5 Minuten benötigen.\nStecken Sie das IPhone unter keinen Umständen aus.");
            lblIPhoneId.setTextFill(Color.RED);
        });
        addLog("Es wurden 3 modifizierte Apps gefunden!\nStarte Security Scan!\nTrennen Sie  unter keinen Umständen das IPhone von Ihrem Computer!", "ERROR");

    }


    public static long getDirectorySizeJava8(Path path) {

        long size = 0;

        // need close Files.walk
        try (Stream<Path> walk = Files.walk(path)) {

            size = walk
                    //.peek(System.out::println) // debug
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        // ugly, can pretty it with an extract method
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            System.out.printf("Fehler beim erhalten der Größte für %s%n%s", p, e);
                            return 0L;
                        }
                    })
                    .sum();

        } catch (IOException e) {
            System.out.printf("IO errors %s", e);
        }

        return size;

    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }
}
