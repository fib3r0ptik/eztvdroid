package com.hamaksoftware.eztvpal.fragments;

public interface IAsyncTaskListener<T> {
    /* use this to pass data from any thread.
     * make sure that observers show this data on UI thread.
     */
    public void onTaskCompleted(T data,String ASYNC_ID);

    /* use this to update UI that the asycn is still working
     * usually used with a progress dialog with indeterminate true (no progress bar).
     */
    public void onTaskWorking(String ASYNC_ID);

    /* use this to update UI of the progress of the loop (in case we are using huge list)
     * usually used with a progress dialog with indeterminate false to display a progress bar.
     */
    public void onTaskProgressUpdate(int progress,String ASYNC_ID);


    /* use this to update UI of the count of the list (in case we are using huge list)
     * usually used with a progress dialog with indeterminate false to display a progress bar.
     */
    public void onTaskProgressMax(int max,String ASYNC_ID);

    /* use this to update UI of just about any messages you want.
     * usually used with a progress dialog.
     */
    public void onTaskUpdateMessage(String message,String ASYNC_ID);

    /* inform the UI thread for errors occured.
     * TODO: find a way to avoid passing generic exception (if there is a way)
     */
    public void onTaskError(Exception e,String ASYNC_ID);
}
