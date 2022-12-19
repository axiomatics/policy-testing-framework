package extra

class MyLogger extends ByteArrayOutputStream {

    @Override
    public void flush() {
        println this.toString().replaceAll("WARNING", "warn")
        reset();
    }
}