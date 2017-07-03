package core.resolver;

import core.data.AILevel;
import core.interfaces.IBoard;
import core.interfaces.IGameCoreResolver;

public class ResolveCreator {
    public static IGameCoreResolver BuildEVP(IBoard board, AILevel level){
        return new EVPResolver(board, level);
    }

    public static IGameCoreResolver BuildPVP(IBoard board){
        return new PVPResolver(board);
    }

}
