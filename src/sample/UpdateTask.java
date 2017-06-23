package sample;

import javafx.concurrent.Task;

public class UpdateTask extends Task {

    private final Runnable work;

    public UpdateTask(Runnable work) {
        this.work = work;
    }

    @Override
    protected Object call() throws Exception {
        work.run();
        return null;
    }
}
