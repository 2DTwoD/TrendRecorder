package org.goznak.Instruments;

import javax.swing.*;

public class Dialogs extends JOptionPane {

    public static void messageDialog(String text){
        showMessageDialog(null, text, "Внимание", INFORMATION_MESSAGE);
    }

    public static void errorDialog(String text){
        showMessageDialog(null, text, "Ошибка", ERROR_MESSAGE);
    }

    public static int confirmDialog(String text){
        return showConfirmDialog(null, text, "Подтверждение", OK_CANCEL_OPTION);
    }
}
