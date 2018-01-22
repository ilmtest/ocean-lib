package com.ocean.lib.io;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;

public class DBUtils
{
	private static final String SUFFIX_NOT_NULL = " NOT NULL";


	public static void createTable(Connection c, String table, Collection<String> columns) throws SQLException {
		execStatement(c, "CREATE TABLE IF NOT EXISTS "+table+" ("+StringUtil.join(columns, ",")+")");
	}


	public static final void execStatement(Connection c, String query) throws SQLException
	{
		PreparedStatement ps = c.prepareStatement(query);
		ps.execute();
		ps.close();
	}
	
	
	/**
	 * Executes the statement and then returns the generated ID of the row.
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	public static int execAndGetId(PreparedStatement ps) throws SQLException
	{
		ps.execute();
		ps.getConnection().commit();
		ResultSet rs = ps.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
	}


	public static List<String> isolateColumnNames(List<String> columns, String... toRemove)
	{
		//TODO: Bring all the manual usages to use this one

		for (int i = columns.size()-1; i >= 0; i--) {
			columns.set( i, columns.get(i).split(" ")[0] );
		}

		columns.removeAll( Arrays.asList(toRemove) );
		return columns;
	}


	public static void cleanUp(String db)
	{
		new File(db).delete();
		new File(db+"-journal").delete();
	}


	public static void setNullInt(int i, int value, PreparedStatement ps) throws SQLException
	{
		if (value == 0) {
			ps.setNull(i, Types.INTEGER);
		} else {
			ps.setInt(i, value);
		}
	}


	/**
	 * Creates a list of columns for a table that cannot be NULL.
	 * @param primary A primary key. If this is <code>null</code> then a primary key is not created.
	 * @param columns All additional mandatory columns for the table.
	 * @return
	 */
	public static List<String> createNotNullColumns(String primary, String... columns)
	{
		ArrayList<String> result = new ArrayList<String>();

		if (primary != null) {
			result.add(primary+" PRIMARY KEY");
		}

		for (String column: columns) {
			result.add(column+SUFFIX_NOT_NULL);
		}

		return result;
	}
	
	
	/**
	 * All the columns one wishes to format for a statement.
	 * @param columns
	 * @return
	 */
	public static String formatColumns(String ...columns)
	{
		return StringUtils.join(columns,",");
	}
	
	
	public static PreparedStatement createQuery(Connection c, String table, String where, String orderColumn, String ...columns) throws SQLException
	{
		String query = "SELECT "+formatColumns(columns)+" FROM "+table;
		
		if (where != null) {
			query += " WHERE ("+where+")";
		}
		
		if (orderColumn != null) {
			query += " ORDER BY "+orderColumn;
		}
		
		return c.prepareStatement(query);
	}
	
	
	public static int insertOrIgnore(Connection c, String table, String[] columns, Object ...values) throws SQLException
	{
		if (columns.length != values.length) {
			throw new IllegalArgumentException("Column count "+columns.length+" does not match value count "+values.length);
		}
		
		PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO "+table+" ("+formatColumns(columns)+") VALUES ("+StringUtils.repeat("?",",",columns.length)+")");
		int i = 0;
		
		for (Object o: values)
		{
			if (o instanceof Integer) {
				ps.setInt(++i, (int)o);
			} else if (o instanceof String) {
				ps.setString(++i, (String)o);
			} else {
				throw new IllegalArgumentException("Unknown value type: "+o.toString());
			}
		}
		
		return execAndGetId(ps);
	}


	/**
	 * Appends a list of columns to <code>result</code> that are allowed to be <code>null</code>.
	 * @param result The list of columns to append the results to.
	 * @param columns All additional optional columns.
	 * @return A reference to <code>result</code>.
	 */
	public static List<String> createNullColumns(List<String> result, String... columns)
	{
		for (String column: columns) {
			result.add(column);
		}

		return result;
	}


	public static List<String> filterNullable(List<String> columns)
	{
		columns = columns.stream().filter(p -> p.endsWith(SUFFIX_NOT_NULL)).collect(Collectors.toList());

		for (int i = 0; i < columns.size(); i++) {
			columns.set(i, columns.get(i).split(" ")[0]);
		}

		return columns;
	}


	public static PreparedStatement createInsert(Connection c, String table, List<String> columns) throws SQLException {
		return c.prepareStatement("INSERT INTO "+table+" ("+StringUtils.join(columns,",")+")"+" VALUES "+DBUtils.generatePlaceHolders(columns));
	}


	public static void attach(Connection c, String file, String name) throws SQLException
	{
		System.out.println("Attaching: "+file);
		execStatement(c, "ATTACH DATABASE '"+file+"' AS "+name);
	}

	public static void detach(Connection c, String name) throws SQLException {
		execStatement(c, "DETACH DATABASE "+name);
	}


	public static String generatePlaceHolders(Collection<String> columns)
	{
		Collection<String> placeHolders = new ArrayList<String>();

		for (int i = columns.size()-1; i >= 0; i--) {
			placeHolders.add("?");
		}

		return "("+StringUtil.join(placeHolders, ",")+")";
	}
}