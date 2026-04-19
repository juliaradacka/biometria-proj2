package biometria.gui;

import biometria.operations.morphology.*;
import biometria.operations.point.*;
import javax.swing.*;

public class MenuFactory {

    public static JMenu createFileMenu(MainFrame frame) {
        JMenu fileMenu = new JMenu("Plik");

        JMenuItem openItem = new JMenuItem("Otwórz plik");
        openItem.addActionListener(e -> frame.openFile());
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Zapisz jako");
        saveItem.addActionListener(e -> frame.saveFile());
        fileMenu.add(saveItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Zamknij program");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        return fileMenu;
    }

    public static JMenu createEditMenu(MainFrame frame) {
        JMenu editMenu = new JMenu("Edycja");

        JMenuItem undoItem = new JMenuItem("Cofnij");
        undoItem.addActionListener(e -> {
            frame.undo();
            frame.refreshView();
        });
        editMenu.add(undoItem);

        JMenuItem redoItem = new JMenuItem("Ponów");
        redoItem.addActionListener(e -> {
            frame.redo();
            frame.refreshView();
        });
        editMenu.add(redoItem);

        editMenu.addSeparator();

        JMenuItem resetItem = new JMenuItem("Resetuj do oryginału");
        resetItem.addActionListener(e -> {
            frame.reset();
            frame.refreshView();
        });
        editMenu.add(resetItem);

        return editMenu;
    }
}