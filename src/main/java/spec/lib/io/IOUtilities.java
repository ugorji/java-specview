package spec.lib.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/** Class with some IO utilities methods */
public class IOUtilities {

  /**
   * select a file that we can write to if the file chooser passed is null, we create a new
   * JFileChooser
   */
  public static final File selectWritableFile(JFileChooser fileChooser) {
    if (fileChooser == null) fileChooser = new JFileChooser();

    fileChooser.setSelectedFile(null);
    int returnVal = fileChooser.showSaveDialog(fileChooser);
    if (returnVal != JFileChooser.APPROVE_OPTION) return null;

    File file = fileChooser.getSelectedFile();
    File parentFile = file.getParentFile();

    if (file == null) {
      JOptionPane.showMessageDialog(
          fileChooser, "No file was selected", "Error writing file", JOptionPane.ERROR_MESSAGE);
      return null;
    } else if (!(parentFile.canWrite())) {
      JOptionPane.showMessageDialog(
          fileChooser,
          "You cannot write to a file in this directory:\n" + parentFile.getAbsolutePath(),
          "Error writing file",
          JOptionPane.ERROR_MESSAGE);
      return null;
    } else if (file.exists()) {
      int value =
          JOptionPane.showConfirmDialog(
              fileChooser,
              "The file, " + file.getAbsolutePath() + "\n    exists, ... overwrite it???",
              "Overwrite file?",
              JOptionPane.OK_CANCEL_OPTION);

      if (value != JOptionPane.OK_OPTION) return null;
    }

    return file;
  }

  /**
   * select a file that we can read from if the file chooser passed is null, we create a new
   * JFileChooser
   */
  public static final File selectReadableFile(JFileChooser fileChooser) {
    if (fileChooser == null) fileChooser = new JFileChooser();

    fileChooser.setSelectedFile(null);
    int returnVal = fileChooser.showOpenDialog(fileChooser);
    if (returnVal != JFileChooser.APPROVE_OPTION) return null;

    File file = fileChooser.getSelectedFile();
    if (file == null) {
      JOptionPane.showMessageDialog(
          fileChooser, "No file was selected", "Error writing file", JOptionPane.ERROR_MESSAGE);
      return null;
    } else if (!(file.canRead())) {
      JOptionPane.showMessageDialog(
          fileChooser,
          "You cannot read this file:\n " + file.getAbsolutePath(),
          "Error reading file",
          JOptionPane.ERROR_MESSAGE);
      return null;
    } else if (!(file.exists())) {
      JOptionPane.showMessageDialog(
          fileChooser,
          "You cannot read this file:\n " + file.getAbsolutePath(),
          "Error reading file",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }

    return file;
  }

  /** save some string to a file ... return true/false to indicate success */
  public static boolean save(String contents, JFileChooser fileChooser) {
    File file = selectWritableFile(fileChooser);
    if (file == null) return false;

    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(contents);
      out.newLine();
      out.close();
      return true;
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
      ioe.printStackTrace();
      return false;
    }
  }
}
