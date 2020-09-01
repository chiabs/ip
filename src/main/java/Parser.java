import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Parser {
    private Ui ui;

    public Parser(Ui ui) {
        this.ui = ui;
    }

    /**
     * Processes inputs from the user.
     * @param tasks TaskList containing all tasks.
     * @param filePath for path of file.
     */
    public void parser(TaskList tasks, String filePath) {
        Scanner scanner = new Scanner(System.in);
        Path path = Paths.get(filePath);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("bye")) {
                ui.bye();
                break;
            } else if (line.equals("list")) {
                ui.list(tasks);
            } else {
                String[] words = line.split("\\s+");
                if (words[0].equals("done")) {
                    try {
                        int index = completeDelete(line, tasks.size());
                        tasks.get(index).setDone();
                        Storage.fileUpdate(tasks, path);
                        ui.done(tasks, index);
                    } catch (DukeException e) {
                        System.out.println(e.getMessage());
                    }
                } else if (words[0].equals("delete")) {
                    try {
                        int index = completeDelete(line, tasks.size());
                        ui.remove(tasks, index);
                        tasks.delete(index);
                        Storage.fileUpdate(tasks, path);
                    } catch (DukeException e) {
                        System.out.println(e.getMessage());
                    }
                } else if (words[0].equals("find")) {
                    if (words.length > 1) {
                        String keyword = line.substring(5);
                        TaskList matches = matchFinder(tasks, keyword);
                        ui.foundMatches(matches);
                    }
                } else {
                    try {
                        Task task = taskClassify(line);
                        tasks.add(task);
                        Storage.fileUpdate(tasks, path);
                        ui.add(task, tasks.size());
                    } catch (DukeException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }


    public String parser(String line, TaskList tasks) {
        Path path = Paths.get("./duke.txt");
        if (line.equals("bye")) {
            return "Bye! Hope to see you again ;)";
        } else if (line.equals("list")) {
            int count = 1;
            String string = "";
            for (Task task : tasks.getList()) {
                if (count == tasks.size()) {
                    string += count + "." + task.toString();
                } else {
                    string += count + "." + task.toString() + "\n";
                    count++;
                }
            }
            return string;
        } else {
            String[] words = line.split("\\s+");
            if (words[0].equals("done")) {
                try {
                    int index = completeDelete(line, tasks.size());
                    tasks.get(index).setDone();
                    Storage.fileUpdate(tasks, path);
                    String string = "Nice! I've marked this task as done:" + "\n";
                    string += tasks.get(index).toString();
                    return string;
                } catch (DukeException e) {
                    return e.getMessage();
                }
            } else if (words[0].equals("delete")) {
                try {
                    int index = completeDelete(line, tasks.size());
                    String string = "Noted. I've removed this task:" + "\n";
                    string += tasks.get(index).toString() + "\n";
                    string += "Now you have " + (tasks.size() - 1) + " tasks in the list.";
                    tasks.delete(index);
                    Storage.fileUpdate(tasks, path);
                    return string;
                } catch (DukeException e) {
                    return e.getMessage();
                }
            } else if (words[0].equals("find")) {
                if (words.length > 1) {
                    String keyword = line.substring(5);
                    TaskList matches = matchFinder(tasks, keyword);
                    String string = "Here are the matching tasks in your list:" + "\n";
                    int counter = 1;
                    for (Task task : matches.getList()) {
                        if (counter == tasks.size()) {
                            string += counter + "." + task.toString();
                        } else {
                            string += counter + "." + task.toString() + "\n";
                            counter++;
                        }
                    }
                    return string;
                } else {
                    return "OOPS!! Missing keyword to find!";
                }
            } else {
                try {
                    Task task = taskClassify(line);
                    tasks.add(task);
                    Storage.fileUpdate(tasks, path);
                    String string = "Got it. I've added this task:" + "\n";
                    string += "  " + task.toString() + "\n";
                    string += "Now you have " + tasks.size() + " tasks in the list.";
                    return string;

                } catch (DukeException e) {
                    return e.getMessage();
                }
            }
        }
    }


    /**
     * returns the index of task to be deleted/completed if possible.
     * @param str input line from user.
     * @param numTask total number of tasks.
     * @return int index of task in TaskList.
     * @throws DukeException if input is invalid or out of bounds.
     */
    public static int completeDelete(String str, int numTask) throws DukeException {
        String[] words = str.split("\\s+");
        int len = words.length;
        if (len == 2) {
            String num = words[1];
            boolean result = num.matches(".*\\d.*");
            if (result) {
                int index = Integer.parseInt(num) - 1;
                if (index >= numTask || index < 0) {
                    throw new DukeException("OOPS!!! Out of bounds of the list of tasks.");
                }
                return index;
            }
        }
        throw new DukeException("OOPS!!! Invalid task provided.");
    }

    public static TaskList reader(File file) {
        try {
            Scanner s = new Scanner(file);
            TaskList tasks = new TaskList();
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String splitOn = "\\s*@\\s*";
                String[] words = line.split(splitOn);
                int done = Integer.parseInt(words[1]);
                if (words.length == 3) {
                    ToDo toDo = new ToDo(words[2]);
                    if (done == 1) {
                        toDo.setDone();
                    }
                    tasks.add(toDo);
                } else {
                    if (words[0].equals("[E]")) {
                        Event event = new Event(words[2], words[3]);
                        if (done == 1) {
                            event.setDone();
                        }
                        tasks.add(event);
                    } else {
                        Deadline deadline = new Deadline(words[2], words[3]);
                        if (done == 1) {
                            deadline.setDone();
                        }
                        tasks.add(deadline);
                    }
                }
            }
            return tasks;
        } catch (FileNotFoundException e) {
            System.out.println("OOPS!! Something went wrong :(");
            return new TaskList();
        }
    }

    public static TaskList matchFinder(TaskList tasks, String keyword) {
        TaskList matches = new TaskList();
        for (Task task : tasks.getList()) {
            String desc = task.desc;
            if (desc.contains(keyword)) {
                matches.add(task);
            }
        }
        return matches;
    }

    /**
     * Returns the task of correct type based on input.
     * @param str input by user.
     * @return task.
     * @throws DukeException for any invalid inputs.
     */
    public static Task taskClassify(String str) throws DukeException {
        String[] words = str.split("\\s+");
        int len = words.length;

        switch (words[0]) {
            case "todo":
                if (len == 1) {
                    throw new DukeException("OOPS!!! The description of a todo cannot be empty.");
                } else {
                    String desc = "";
                    for (int i = 1; i < len; i++) {
                        if (i == len - 1) {
                            desc += words[i];
                            break;
                        }
                        desc += words[i] + " ";
                    }
                    return new ToDo(desc);
                }
            case "deadline":
                if (len == 1) {
                    throw new DukeException("OOPS!!! The description of a deadline cannot be empty.");
                } else {
                    String desc = "";
                    String time = "";
                    int count = 0;
                    for (int i = 1; i < len; i++) {
                        if (words[i].equals("/by")) {
                            count = i + 1;
                            desc = desc.substring(0, desc.length() - 1);
                            break;
                        }
                        desc += words[i] + " ";
                    }
                    if (count == 0 || count == len) {
                        throw new DukeException("OOPS!!! The date/time of a deadline cannot be empty.");
                    }
                    for (int j = count; j < len; j++) {
                        if (j == len - 1) {
                            time += words[j];
                            break;
                        }
                        time += words[j] + " ";
                    }
                    return new Deadline(desc, time);
                }
            case "event":
                if (len == 1) {
                    throw new DukeException("OOPS!!! The description of a deadline cannot be empty.");
                } else {
                    String desc = "";
                    String time = "";
                    int count = 0;
                    for (int i = 1; i < len; i++) {
                        if (words[i].equals("/at")) {
                            count = i + 1;
                            desc = desc.substring(0, desc.length() - 1);
                            break;
                        }
                        desc += words[i] + " ";
                    }
                    if (count == 0 || count == len) {
                        throw new DukeException("OOPS!!! The date/time of a deadline cannot be empty.");
                    }
                    for (int j = count; j < len; j++) {
                        if (j == len - 1) {
                            time += words[j];
                            break;
                        }
                        time += " " + words[j];
                    }
                    return new Event(desc, time);
                }
            default:
                throw new DukeException("OOPS!!! I'm sorry, but I don't know what that means :-(");
        }
    }
}
