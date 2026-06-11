@Override
public void onCreate() {
    super.onCreate();
    
    energyModel.c_actors.add(this);
    energyModel.c_connectionOwners.add(this);
}
