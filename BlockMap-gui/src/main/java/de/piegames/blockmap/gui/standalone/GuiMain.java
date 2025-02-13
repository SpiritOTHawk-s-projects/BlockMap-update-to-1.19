package de.piegames.blockmap.gui.standalone;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class GuiMain extends Application {

	private static Log log = null;

	/** Lazily initialize the logger to avoid loading Log4j too early (startup performance). */
	private static void checkLogger() {
		if (log == null)
			log = LogFactory.getLog(GuiMain.class);
	}

	/** Internal API, public due to technical reasons */
	public static GuiMain	instance;
	/** Internal API, public due to technical reasons */
	public GuiController	controller;
	/** Internal API, public due to technical reasons */
	public Stage			stage;

	private Parent			root;

	public GuiMain() {
	}

	@Override
	public void init() throws IOException {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("scene.fxml"));
			root = (Parent) loader.load();
			controller = (GuiController) loader.getController();
		} catch (Throwable t) {
			checkLogger();
			log.fatal("Cannot start BlockMap", t);
			System.exit(-1);
		}
	}

	@Override
	public void start(Stage stage) throws IOException {
		try {
			this.stage = stage;
			stage.setTitle("BlockMap map viewer");
			stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));

			Scene scene = new Scene(root, 700, 450);
			scene.getStylesheets().add("/de/piegames/blockmap/gui/standalone/style.css");
			stage.setScene(scene);
			stage.show();
			scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN), () -> controller.exit());

			GuiMainPreloader.splashScreen.hide();

			/* Put this last to guarantee that the application is fully initialized once instance!=null. */
			instance = this;
		} catch (Throwable t) {
			checkLogger();
			log.fatal("Cannot start BlockMap", t);
			System.exit(-1);
		}

		var parameters = getParameters().getUnnamed();
		if (parameters.contains("-v") || parameters.contains("--verbose")) {
			Configurator.setRootLevel(Level.DEBUG);
			checkLogger();
			log.debug("Debug logging enabled.");
		}
		/* Load a world on start if specified on the command line */
		parameters
				.stream()
				.filter(p -> !"-v".equals(p) && !"--verbose".equals(p))
				.findFirst()
				.ifPresentOrElse(world -> {
					checkLogger();
					log.info("Loading path specified on command line");
					controller.load(world);
				}, () -> {
					controller.showLoadDialog();
				});
	}

	@Override
	public void stop() {
		controller.shutDown();
	}

	public static void main(String... args) {
		/* Without this, JOML will print vectors out in scientific notation which isn't the most human readable thing in the world */
		System.setProperty("joml.format", "false");
		System.setProperty("javafx.preloader", GuiMainPreloader.class.getCanonicalName());
		Application.launch(args);
	}
}