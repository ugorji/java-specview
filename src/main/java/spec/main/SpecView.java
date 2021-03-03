package spec.main;

import java.io.File;
import java.util.Properties;
import javax.swing.JOptionPane;
import spec.lib.graph.Graph;
import spec.ui.DefaultSpecUI_MDI;

public class SpecView {

  public static final String infoMessage;

  static {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("**************************************************\n");
    strBuf.append("Usage\n=====\n");
    strBuf.append("java -DSpecView.install.dir=<SpecView Installation Dir>spec.main.SpecView \n\n");
    // strBuf.append (" -DSpecView.user.dir=<SpecView User DIr>\n\n");
    strBuf.append("**************************************************\n");

    infoMessage = strBuf.toString();
  }

  public static String ensureUserDir() {
    String userDir = System.getProperty("SpecView.user.dir");
    if (userDir == null) {
      System.out.println(infoMessage);
      throw new RuntimeException("SpecView.user.dir need to be put into System properties");
    }
    if (!(userDir.endsWith("/"))) userDir = userDir + "/";

    // File parentUserDirFileObj = userDirFileObj.getParentFile();
    File userDirFileObj = new File(userDir);
    if (!(userDirFileObj.exists())) {
      String userDirAbsolutePath = userDirFileObj.getAbsolutePath();
      int value =
          JOptionPane.showConfirmDialog(
              null,
              "The user directory: "
                  + userDirAbsolutePath
                  + "\n entered does not exist.\n"
                  + "It is needed to store some settings and other SpecView things.\n"
                  + "Shall we create it now (Cancel means exit)",
              "Error starting SpecView",
              JOptionPane.OK_CANCEL_OPTION);

      if (value != JOptionPane.OK_OPTION)
        throw new RuntimeException("The SpecView user directory does not exist");

      boolean userDirCreated = userDirFileObj.mkdir();
      if (!userDirCreated)
        throw new RuntimeException(
            "The SpecView user directory could not be created" + userDirAbsolutePath);
    }

    if (!(userDirFileObj.canWrite()))
      throw new RuntimeException("The SpecView user directory cannot be written to");

    return userDir;
  }

  public static String ensureInstallDir() {
    String installDir = System.getProperty("SpecView.install.dir");
    // || userDir == null    and SpecView.user.dir
    if (installDir == null) {
      System.out.println(infoMessage);
      throw new RuntimeException("SpecView.install.dir need to be put into System properties");
    }

    if (!(installDir.endsWith("/"))) installDir = installDir + "/";

    // ensure that the user directory and install directory are created
    File installDirFileObj = new File(installDir);
    if (!(installDirFileObj.exists()))
      throw new RuntimeException("The SpecView install directory passed does not exist");

    return installDir;
  }

  /** convenience method so this can be run as a stand-alone application */
  public static void main(String[] args) {
    try {
      // String installDir = ensureInstallDir();
      // String userDir = ensureUserDir();

      Properties graphProperties = new Properties();
      graphProperties.load(
          SpecView.class
              .getClassLoader()
              .getResourceAsStream("spec/resources/lib/Graph.properties"));
      Graph.loadIntoProperties(graphProperties);

      Properties properties = new Properties();
      properties.putAll(System.getProperties());
      properties.load(
          SpecView.class
              .getClassLoader()
              .getResourceAsStream("spec/resources/lib/SpecView.properties"));

      DefaultSpecUI_MDI specUI = new DefaultSpecUI_MDI(properties, true);
    }
    // catch (MissingResourceException mre) {
    // catch (FileNotFoundException fnfe) {
    catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
