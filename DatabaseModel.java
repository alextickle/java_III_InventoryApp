import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseModel implements Modelable{
	private Viewable view;
	final String DATABASE_URL = "jdbc:postgresql:crud";
	final String username = "postgres";
	final String password = "password";
    private ArrayList<String[]> lastSearchParams;
	
	public void setView(Viewable view){
		this.view = view;
	}
	
	
	
	public void runQuery(String query){
		try {
			Connection connection = DriverManager.getConnection(
		            DATABASE_URL, username, password); 
	        Statement statement = connection.createStatement(); 
	        statement.executeQuery(query);
		}
        catch (SQLException sqlException){                                                                  
	         sqlException.printStackTrace();
	    }   
	}
	
	public void updateSelf(Command command){
		MediaItem item = command.getMediaItem();
		String query;
		int id = command.getQueryIndex();
		switch (command.getType()){
			case CREATE:
				query = String.format("INSERT INTO inventory (mediatype, title, artist) VALUES ('%s', '%s', '%s')",
						item.getMediaType(), item.getTitle(), item.getArtist());
				runQuery(query);
				break;
			case SEARCH:
				ArrayList<String[]> searchParameters = new ArrayList<String[]>();
				String[] array = new String[2];
				String parameter = item.getTitle();
				if (searchParameters.size() == 0){
					query = "SELECT * FROM inventory";
					runQuery(query);
					lastSearchParams = searchParameters;
				}
				else {
					if (!parameter.equals("")){
						array = new String[]{"title", parameter};
						searchParameters.add(array);
					}
					parameter = item.getArtist();
					if (!parameter.equals("")){
						array = new String[]{"artist", parameter};
						searchParameters.add(array);
					}

					parameter = item.getId();
					if (!parameter.equals("")){
						array = new String[]{"id", parameter};
						searchParameters.add(array);
					}

					parameter = item.getMediaType();
					if (!parameter.equals("")){
						array = new String[]{"mediatype", parameter};
						searchParameters.add(array);
					}
					lastSearchParams = searchParameters;
					int numOfParams = searchParameters.size();
					query = "SELECT * FROM inventory WHERE ";
					for (int i = 0; i < numOfParams - 1; i++){
						query += String.format("%s = '%s' AND ",
								searchParameters.get(i)[0],
								searchParameters.get(i)[1]);
					}
					query += String.format("%s = '%s'", 
							searchParameters.get(numOfParams - 1)[0],
							searchParameters.get(numOfParams - 1)[1]);
					
					if (searchParameters.size() == 0){
						query = "SELECT * FROM inventory";
					}
					runQuery(query);
				}
				break;
			case UPDATE:
				if (!item.getTitle().equals("")){
					if (!item.getArtist().equals("")){
						query = "UPDATE inventory SET title = '" + item.getTitle() +
							"', artist = '" + item.getArtist() + "' WHERE id = " + id;
					}
					else {
						query = "UPDATE inventory SET title = '" + item.getTitle() +
								"' WHERE id = " + id;
					}
				}
				else {
					query = "UPDATE inventory SET artist = '" + item.getArtist() +
							"' WHERE id = " + id;
				}
				System.out.println("query:" + query);
				runQuery(query);
				break;
			case DELETE:
				query = "DELETE FROM inventory WHERE id = " + id;
				System.out.println("query: " + query);
				runQuery(query);
				break;
		}
		alertView();
	}

	public void sendUpdatedInfoToView(Command currentCommand){
		String query = "";
		ResultSetTableModel tableModel;
		if (currentCommand.getType() == Command.Type.UPDATE || 
			currentCommand.getType() == Command.Type.DELETE ||
			currentCommand.getType() == Command.Type.SEARCH){
			int numOfParams = lastSearchParams.size();
			if (numOfParams == 0){
				query = "SELECT * FROM inventory";
			}
			else {
				query = "SELECT * FROM inventory WHERE ";
				for (int i = 0; i < numOfParams - 1; i++){
					query += String.format("%s = '%s' AND ",
							lastSearchParams.get(i)[0],
							lastSearchParams.get(i)[1]);
				}
				query += String.format("%s = '%s'", 
						lastSearchParams.get(numOfParams - 1)[0],
						lastSearchParams.get(numOfParams - 1)[1]);
			}
			try {
				tableModel = new ResultSetTableModel(DATABASE_URL, username, password, query);
				view.updateSelf(tableModel);
			}
			catch (SQLException sqlException){                                                                  
		         sqlException.printStackTrace();
		    }
		}
		else {
			view.updateSelf(new Object());
		}
		
		 
	}

	public void alertView(){
		view.requestInfoFromModel();
	}

}
