package ee.taltech.gandalf.sweeper;

public class SweeperClock implements Runnable {
    private volatile boolean running = true;
    private final SweepingState state;

    public SweeperClock(SweepingState state) {
        this.state = state;
    }

    /**
     * The main loop of the TPS method.
     * This loop is run TPS/s.
     */
    @Override
    public void run() {
        // TPS means ticks per second.
        double tps = 240;
        // Time since last tick.
        double delta = 0;
        // Time since last update
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            // Logic to implement TPS in a second.
            delta += (now - lastTime) / (1000000000.0 / tps);
            lastTime = now;
            while (delta >= 1) {
                // Tick() to send out a tick calculation.
                tick();
                delta--;
            }
        }
    }

    /**
     * Method is called out every tick (in run() method).
     * Contains logic, that needs to be updated every tick.
     */
    public void tick() {
        System.out.println("This thing is working?");
    }
}
