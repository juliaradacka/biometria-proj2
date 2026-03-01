package biometria.service;

import biometria.io.Images;
import biometria.model.ImageMatrix;
import biometria.operations.ImageOperation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class EditorService {

    private static final int MAX_HISTORY_SIZE = 20;

    private ImageMatrix original;
    private ImageMatrix current;
    private final Deque<ImageMatrix> undoStack = new ArrayDeque<>();
    private final Deque<ImageMatrix> redoStack = new ArrayDeque<>();

    public void loadImage(File file) throws IOException {
        this.original = Images.loadImage(file);
        this.current = original.copy();
        undoStack.clear();
        redoStack.clear();
    }

    public void saveImage(File file) throws IOException {
        if (current == null) {
            throw new IllegalStateException("Brak obrazu do zapisania");
        }
        Images.saveImage(current, file);
    }

    public void applyOperation(ImageOperation op) {
        if (current == null) {
            throw new IllegalStateException("Brak załadowanego obrazu");
        }

        undoStack.push(current.copy());

        if (undoStack.size() > MAX_HISTORY_SIZE) {
            undoStack.removeLast();
        }

        redoStack.clear();
        current = op.apply(current);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(current);
            if (redoStack.size() > MAX_HISTORY_SIZE) {
                redoStack.removeLast();
            }
            current = undoStack.pop();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(current);
            if (undoStack.size() > MAX_HISTORY_SIZE) {
                undoStack.removeLast();
            }
            current = redoStack.pop();
        }
    }

    public void resetToOriginal() {
        if (original != null) {
            undoStack.clear();
            redoStack.clear();
            current = original.copy();
        }
    }

    public ImageMatrix getCurrent() {
        return current;
    }

    public ImageMatrix getOriginal() {
        return original;
    }

    public boolean hasImage() {
        return current != null;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}