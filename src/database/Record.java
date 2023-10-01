package database;

public class Record {

    private long GAME_DATE_EST;
    private int TEAM_ID_home;
    private int PTS_HOME;
    private float FG_PCT_home;
    private float FT_PCT_home;
    private float FG3_PCT_home;
    private int AST_home;
    private int REB_home;
    private int HOME_TEAM_WINS;

    public Record(long GAME_DATE_EST, int TEAM_ID_home, int PTS_HOME, float FG_PCT_home, float FT_PCT_home,
                  float FG3_PCT_home, int AST_home, int REB_home, int HOME_TEAM_WINS) {
        this.GAME_DATE_EST = GAME_DATE_EST;
        this.TEAM_ID_home = TEAM_ID_home;
        this.PTS_HOME = PTS_HOME;
        this.FG_PCT_home = FG_PCT_home;
        this.FT_PCT_home = FT_PCT_home;
        this.FG3_PCT_home = FG3_PCT_home;
        this.AST_home = AST_home;
        this.REB_home = REB_home;
        this.HOME_TEAM_WINS = HOME_TEAM_WINS;
    }

    @Override
    public String toString() {
        return String.format(
                "Record: GAME DATE EST: %s, TEAM_ID_HOME: %d, PTS_HOME: %d, FG_PCT_home: %f, FT_PCT_home: %f, FG3_PCT_home: %f, AST_home: %d, REB_home: %d, HOME_TEAM_WINS: %d",
                GAME_DATE_EST, TEAM_ID_home,  PTS_HOME,  FG_PCT_home,  FT_PCT_home,
                FG3_PCT_home,  AST_home,  REB_home,  HOME_TEAM_WINS
        );
    }

    public long getGAME_DATE_EST() {
        return GAME_DATE_EST;
    }

    public int getTEAM_ID_home() {
        return TEAM_ID_home;
    }

    public int getPTS_HOME() {
        return PTS_HOME;
    }

    public float getFG_PCT_home() {
        return FG_PCT_home;
    }

    public float getFT_PCT_home() {
        return FT_PCT_home;
    }

    public float getFG3_PCT_home() {
        return FG3_PCT_home;
    }

    public int getAST_home() {
        return AST_home;
    }

    public int getREB_home() {
        return REB_home;
    }

    public int getHOME_TEAM_WINS() {
        return HOME_TEAM_WINS;
    }

}
