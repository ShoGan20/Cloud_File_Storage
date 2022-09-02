package fileSync.model;

public interface Updator {

	public abstract void updateTree();

	public abstract void append(String text);

	public abstract void updateTitle(int number);

}
