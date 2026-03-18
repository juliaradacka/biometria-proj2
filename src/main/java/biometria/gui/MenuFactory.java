package biometria.gui;

import biometria.operations.filter.ConvolutionOperation;
import biometria.operations.filter.RobertsOperation;
import biometria.operations.filter.SobelOperation;
import biometria.operations.point.*;

import javax.swing.*;

public class MenuFactory {

    private static final int BRIGHTNESS_MIN = -255;
    private static final int BRIGHTNESS_MAX = 255;
    private static final int BRIGHTNESS_DEFAULT = 0;

    private static final int CONTRAST_MIN = -255;
    private static final int CONTRAST_MAX = 255;
    private static final int CONTRAST_DEFAULT = 0;

    private static final int BINARIZATION_MIN = 0;
    private static final int BINARIZATION_MAX = 255;
    private static final int BINARIZATION_DEFAULT = 128;

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

    public static JMenu createOperationsMenu(MainFrame frame) {
        JMenu operationsMenu = new JMenu("Operacje");

        JMenuItem grayScaleItem = new JMenuItem("Odcienie szarości");
        grayScaleItem.addActionListener(e -> frame.applyOperation(new GrayScaleOperation()));

        JMenuItem negativeItem = new JMenuItem("Negatyw");
        negativeItem.addActionListener(e -> frame.applyOperation(new NegativeOperation()));

        operationsMenu.add(grayScaleItem);
        operationsMenu.add(negativeItem);

        frame.createParametricOperationItem(
                operationsMenu,
                "Korekta jasności",
                "Dostosuj jasność obrazu",
                BRIGHTNESS_MIN, BRIGHTNESS_MAX, BRIGHTNESS_DEFAULT,
                BrightnessOperation::new
        );

        frame.createParametricOperationItem(
                operationsMenu,
                "Korekta kontrastu",
                "Dostosuj kontrast obrazu",
                CONTRAST_MIN, CONTRAST_MAX, CONTRAST_DEFAULT,
                ContrastOperation::new
        );

        frame.createParametricOperationItem(
                operationsMenu,
                "Binaryzacja",
                "Ustaw próg (threshold) bieli i czerni:",
                BINARIZATION_MIN, BINARIZATION_MAX, BINARIZATION_DEFAULT,
                BinarizationOperation::new
        );

        JMenu convolutionMenu = new JMenu("Filtry Splotowe");

        JMenuItem averageItem = new JMenuItem("Filtr Uśredniający");
        averageItem.addActionListener(e -> {
            if (!frame.validateImageLoaded()) return;
            double[][] mask = {
                    {1, 1, 1},
                    {1, 1, 1},
                    {1, 1, 1}
            };
            frame.applyOperation(new ConvolutionOperation(mask, 9.0));
        });
        convolutionMenu.add(averageItem);

        JMenuItem gaussianItem = new JMenuItem("Filtr Gaussa");
        gaussianItem.addActionListener(e -> {
            if (!frame.validateImageLoaded()) return;
            double[][] mask = {
                    {1, 2, 1},
                    {2, 4, 2},
                    {1, 2, 1}
            };
            frame.applyOperation(new ConvolutionOperation(mask, 16.0));
        });
        convolutionMenu.add(gaussianItem);

        JMenuItem sharpenItem = new JMenuItem("Wyostrzanie");
        sharpenItem.addActionListener(e -> {
            if (!frame.validateImageLoaded()) return;
            double[][] mask = {
                    { 0, -1,  0},
                    {-1,  5, -1},
                    { 0, -1,  0}
            };
            frame.applyOperation(new ConvolutionOperation(mask, 1.0));
        });
        convolutionMenu.add(sharpenItem);

        convolutionMenu.addSeparator();
        JMenuItem sobelItem = new JMenuItem("Wykrywanie krawędzi (Sobela)");
        sobelItem.addActionListener(e ->{
            if(!frame.validateImageLoaded()) return;
            frame.applyOperation(new SobelOperation());
        });
        convolutionMenu.add(sobelItem);

        JMenuItem robertsItem = new JMenuItem("Wykrywanie krawędzi (Roberts)");
        robertsItem.addActionListener(e -> {
            if (!frame.validateImageLoaded()) return;
            frame.applyOperation(new RobertsOperation());
        });
        convolutionMenu.add(robertsItem);

        operationsMenu.addSeparator();
        operationsMenu.add(convolutionMenu);

        JMenuItem customKernelItem = new JMenuItem("Filtr własny");
        customKernelItem.addActionListener(e -> {
            if (!frame.validateImageLoaded()) return;

            CustomConvolutionDialog.Kernel k = CustomConvolutionDialog.showDialog(frame);
            if (k == null) return;

            frame.applyOperation(new ConvolutionOperation(k.mask, k.weight));
        });

        convolutionMenu.addSeparator();
        convolutionMenu.add(customKernelItem);

        return operationsMenu;
    }
}