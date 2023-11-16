package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
private static final String CATEGORY_TABLE = "category";
private static final String MATERIAL_TABLE = "material";
private static final String PROJECT_TABLE = "project";
private static final String PROJECT_CATEGORY_TABLE = "project_category";
private static final String STEP_TABLE = "step";

public Project insertProject(Project project) {
// @formatter:off
	String sql =""
			+ "INSERT INTO " + PROJECT_TABLE + " "
			+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
			+ "VALUES "
			+ "(?, ?, ?, ?, ?)";
	//@formatter:on
	try(Connection conn = DbConnection.getConnection()){
		startTransaction(conn);
	try(PreparedStatement stmt = conn.prepareStatement(sql)) {
		setParameter(stmt, 1, project.getProjectName(), String.class);
		setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
		setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
		setParameter(stmt, 4, project.getDifficulty(), Integer.class);
		setParameter(stmt, 5, project.getNotes(), String.class);

		stmt.executeUpdate();
	Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
	commitTransaction(conn);
	project.setProjectId(projectId);
	return project;
	
	}catch(Exception e) {
		rollbackTransaction(conn);
		throw new DbException(e);
	}
	}catch(SQLException e) {
		throw new DbException(e);
	}//end catch
}//end insertProject

public List<Project> fetchAllProjects() {
String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

try(Connection conn = DbConnection.getConnection()){
	startTransaction(conn);
	try(PreparedStatement stmt = conn.prepareStatement(sql)){
		try(ResultSet rs = stmt.executeQuery()){
			List<Project> projects = new LinkedList<>();
			
			while(rs.next()) {
				projects.add(extract(rs, Project.class));
			}//while
				return projects;
		} //3 try
	}//2 try
	catch(Exception e) {
		rollbackTransaction(conn);
		throw new DbException(e);
	}
}//1 try
	catch(SQLException e) {
		throw new DbException(e);
	}//catch
}//end fetchAllProjects

public Optional<Project> fetchProjectById(Integer projectId) {
String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
try(Connection conn = DbConnection.getConnection()){
	startTransaction(conn);
	try {
		Project project = null;
		try (PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1,projectId, Integer.class);
			try(ResultSet rs = stmt.executeQuery()){
				if(rs.next()) {
				project = extract(rs, Project.class);
			} // if
		}//try 4
		}//try 3
		if(Objects.nonNull(project)) {
			project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
			project.getSteps().addAll(fetchStepsForProject(conn, projectId));
			project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
		}//if
		commitTransaction(conn);
		return Optional.ofNullable(project);
	}//try 2
	catch(Exception e) {
		rollbackTransaction(conn);
		throw new DbException(e);
	}// catch 1
}//try 1
	catch(SQLException e) {
		throw new DbException(e);
	}//catch 2
}//end fetchProjectById

private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) 
		throws SQLException{
	// @formatter:off
	String sql = ""
			+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
			+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
			+ "WHERE project_id = ?";
	//@formatter:on
	try(PreparedStatement stmt = conn.prepareStatement(sql)){
		setParameter(stmt, 1, projectId, Integer.class);
		
		try(ResultSet rs = stmt.executeQuery()){
			List<Category> categories = new LinkedList<>();
		
			while(rs.next()) {
			categories.add(extract(rs, Category.class));
		}//while
		return categories;
		}//try 2
	}//try 1
}// end of fetchCategoriesForProject

private List<Step> fetchStepsForProject(Connection conn, Integer projectId) 
		throws SQLException {
	
		String sql =  "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Step> steps = new LinkedList<>();
			
				while(rs.next()) {
				steps.add(extract(rs, Step.class));
			}//while
			return steps;
			}//try 2
		}//try 1
	}// end of fetchStepsForProject

private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) 
		throws SQLException {
	
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Material> materials = new LinkedList<>();
			
				while(rs.next()) {
				materials.add(extract(rs, Material.class));
			}//while
			return materials;
			}//try 2
		}//try 1
	}// end of fetchMaterialsForProject

public boolean modifyProjectDetails(Project project) {
	//@formatter:off
	String sql = ""
			+ "UPDATE " + PROJECT_TABLE + " SET "
			+ "project_name = ?, "
			+ "estimated_hours = ?, "
			+ "actual_hours = ?, "
			+ "difficulty = ?, "
			+ "notes = ? "
			+ "WHERE project_id = ? ;";
	//@formatter:on
	try(Connection conn = DbConnection.getConnection()){
		startTransaction(conn);
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, project.getProjectName(), String.class);
			setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
			setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
			setParameter(stmt, 4, project.getDifficulty(), Integer.class);
			setParameter(stmt, 5, project.getNotes(), String.class);
			setParameter(stmt, 6, project.getProjectId(), Integer.class);
		
			boolean modified = stmt.executeUpdate() == 1;
			commitTransaction(conn);
			return modified;
		}//try
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}// catch
		}// try
			catch(SQLException e) {
				throw new DbException(e);
			}// catch
}// end of modifyProjectDetails

public boolean deleteProject(Integer projectId) {
	//@formatter:off
	String sql = ""
			+ "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ? ;";
	//@formatter:on
	try(Connection conn = DbConnection.getConnection()){
		startTransaction(conn);
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
		boolean deleted = stmt.executeUpdate() == 1;
			stmt.executeUpdate();
			commitTransaction(conn);
			return deleted;
		}//try
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}// catch
		}// try
			catch(SQLException e) {
				throw new DbException(e);
			}// catch
	
}// end of deleteProject



}//end of class
