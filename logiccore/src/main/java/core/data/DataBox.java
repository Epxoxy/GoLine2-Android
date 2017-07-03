package core.data;
import java.util.Stack;
import java.util.WeakHashMap;
import core.helpers.ArrayHelper;

public class DataBox {

    private int reachableCount;
    private int no = 99;
    private int ok = 1;
    private int[][] datas;
    private int[][] fixEntry;
    private WeakHashMap<Integer,Integer> stepCountor;
    private Stack<DataPoint> undoList;
    private Stack<DataPoint> redoList;

    public DataBox(int[][] entry, int reachable, int denied){
        fixEntry = new int[entry.length][entry[0].length];

        ok = reachable;
        no = denied;
        for (int i = 0; i < entry.length; i++)
            for (int j = 0; j < entry[i].length; j++)
                fixEntry[i][j] = entry[i][j];

        initialize();
    }

    //For check if a point(x][y) can place
    //by the point(x][y) is reachable
    //In 2d array, different to screen coordinate
    //the screen's x coordinate means the 1d of array
    //then screen's y coordinate means then 0d of array
    //so when we check it, notice the different just for the 2d array.
    public boolean isValid(int x, int y, int data){
        return isRecordable()
                && x < datas.length
                && y < datas[0].length
                && datas[x][y] == ok
                && data != no
                && data > ok;
    }

    public boolean isRecordable(){
        return reachableCount > 0;
    }

    public int getReachableCount(){
        return reachableCount;
    }

    public boolean canUndo(){
        return undoList.size() > 0;
    }

    public boolean canRedo(){
        return redoList.size() > 0;
    }

    //Record data if it's valid
    //Record step at the same time
    public boolean record(int x, int y, int data)
    {
        if (recordInternal(x, y, data))
        {
            undoList.push(new DataPoint(x, y, data));
            redoList.clear();
            return true;
        }
        return false;
    }

    //Reset a recorded point to default value
    public boolean reset(int x, int y)
    {
        if(x< datas.length && y < datas[0].length)
        {
            int data = datas[x][y];
            if(data > ok && data != no)
            {
                --reachableCount;
                int v = stepCountor.get(data);
                stepCountor.put(data, v - 1);
                datas[x][y] = ok;
                return true;
            }
        }
        return false;
    }

    public boolean isDataIn(int x, int y, int value)
    {
        int data = datas[x][y];
        return data == value;
    }

    public DataPoint undo()
    {
        if(canUndo())
        {
            DataPoint dp = undoList.pop();
            redoList.push(dp);
            reset(dp.getX(), dp.getY());
            return dp;
        }
        return null;
    }

    public DataPoint Redo()
    {
        if (canRedo())
        {
            DataPoint dp = redoList.pop();
            undoList.push(dp);
            reset(dp.getX(), dp.getY());
            return dp;
        }
        return null;
    }

    public DataPoint peekRedo()
    {
        if (canRedo())
        {
            DataPoint dp = redoList.peek();
            return dp;
        }
        return null;
    }

    public DataPoint peekUndo()
    {
        if (canUndo())
        {
            DataPoint dp = undoList.peek();
            return dp;
        }
        return null;
    }

    public void resetData()
    {
        initialize();
    }

    public int[][] copy()
    {
        return ArrayHelper.copyMatrix(datas);
    }

    private boolean recordInternal(int x, int y, int data)
    {
        //Check if data valid
        if (isValid(x, y, data))
        {
            datas[x][y] = data;
            --reachableCount;

            if (!stepCountor.containsKey(data))
                stepCountor.put(data, 1);
            else
                stepCountor.put(data, stepCountor.get(data)+1);

            return true;
        }
        return false;
    }

    private void initialize()
    {
        reachableCount = 0;
        datas = ArrayHelper.copyMatrix(fixEntry);
        stepCountor = new WeakHashMap<>();
        undoList = new Stack<DataPoint>();
        redoList = new Stack<DataPoint>();

        for (int i = 0; i < datas.length; i++)
            for (int j = 0; j < datas[i].length; j++)
                if (datas[i][j] == ok)
                    ++reachableCount;
    }
}
