import java.io.FileNotFoundException;

public class Duke {
    private TaskList tasks;
    private Ui ui;
    String filePath;

    public Duke(String filePath) throws FileNotFoundException {
        Ui ui = new Ui();
        Storage storage = new Storage(filePath);
        TaskList tasks = storage.readFile();
        this.filePath = filePath;
        this.ui = ui;
        this.tasks = tasks;
    }

    public void run() {
        ui.showWelcome();
        Parser parser = new Parser(ui);
        parser.parser(tasks, filePath);
    }

    public static void main(String[] args) throws FileNotFoundException {
        new Duke("./duke.txt").run();
    }
}
