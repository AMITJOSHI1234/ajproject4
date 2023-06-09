package com.raystec.project4.model;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.raystec.project4.bean.CollegeBean;
import com.raystec.project4.bean.StudentBean;
import com.raystec.project4.exception.ApplicationException;
import com.raystec.project4.exception.DatabaseException;
import com.raystec.project4.exception.DublicateRecordException;
import com.raystec.project4.util.JDBCDataSource;

/**
 * JDBC Implementation of Student Model
 *
 * @author Amit Joshi
 * @version 1.0
 * @Copyright (c) SunilOS
 */


public class StudentModel {

	private static Logger log = Logger.getLogger(StudentModel.class);
	
	/**
     * Find next PK of Student
     *
     * @throws DatabaseException
     */
    public Integer nextPK() throws DatabaseException {
        log.debug("Model nextPK Started");
        Connection conn = null;
        int pk = 0;
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn
                    .prepareStatement("SELECT MAX(ID) FROM ST_STUDENT");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pk = rs.getInt(1);
            }
            rs.close();

        } catch (Exception e) {
            log.error("Database Exception..", e);
            throw new DatabaseException("Exception : Exception in getting PK");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Model nextPK End");
        return pk + 1;
    }
    /**
	 * add record in database
	 * @param bean
	 * @throws ApplicationException
     * @throws DublicateRecordException 
	 *  @throws DuplicateRecordException
	 */
    public long add(StudentBean bean) throws ApplicationException, DublicateRecordException {
    	log.debug("Model add started!!!");
    	
    	Connection conn = null;
    	
    	//get college name
    	CollegeModal cModel = new CollegeModal();
    	CollegeBean collegeBean = cModel.findByPK(bean.getCollegeId());
    	
    	bean.setCollegeName(collegeBean.getName());
    	
    	StudentBean dublicateName = findByEmailId(bean.getEmail());
    	int pk = 0;
    	if(dublicateName!=null) {
    		throw new DublicateRecordException("Email already exits!!!!");
    	}
    	
    	try {
    		conn = JDBCDataSource.getConnection();
    		pk = nextPK();
    		
    		System.out.println(pk + " in ModelJDBC");
    		conn.setAutoCommit(false);//start transaction
    		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO ST_STUDENT VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
			pstmt.setInt(1, pk);
			pstmt.setLong(2, bean.getCollegeId());
			pstmt.setString(3, bean.getCollegeName());
			pstmt.setString(4, bean.getFirstName());
			pstmt.setString(5, bean.getLastName());
			pstmt.setDate(6, new java.sql.Date(bean.getDob().getTime()));
			pstmt.setString(7, bean.getMobileNo());
			pstmt.setString(8, bean.getEmail());
			pstmt.setString(9, bean.getCreatedBy());
			pstmt.setString(10, bean.getModifiedBy());
			pstmt.setTimestamp(11, bean.getCreateDateTime());
			pstmt.setTimestamp(12, bean.getModifiedDatetime());
			pstmt.executeUpdate();

			conn.commit(); // End transaction
			pstmt.close();
    	}catch (Exception e) {
			e.printStackTrace();
			log.error("Database Exception..", e);
			try {
				conn.rollback();

			} catch (Exception ex) {
				ex.printStackTrace();
				throw new ApplicationException("Exception : add rollback exception " + ex.getMessage());
			}
			throw new ApplicationException("Exception : Exception in add Student");
		} finally {
			JDBCDataSource.closeConnection(conn);
		}
		log.debug("Model add End");
		return pk;
	}
    /**
	 * find record by emailid from database
	 * @param email
	 * @return bean
	 * @throws ApplicationException
	 */
    public static StudentBean findByEmailId(String Email) throws ApplicationException {
		log.debug("Model findBy Email Started");
		StringBuffer sql = new StringBuffer("SELECT * FROM ST_STUDENT WHERE EMAIL=?");
		StudentBean bean = null;
		Connection conn = null;
		try {
			conn = JDBCDataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, Email);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				bean = new StudentBean();
				bean.setId(rs.getLong(1));
				bean.setCollegeId(rs.getLong(2));
				bean.setCollegeName(rs.getString(3));
				bean.setFirstName(rs.getString(4));
				bean.setLastName(rs.getString(5));
				bean.setDob(rs.getDate(6));
				bean.setMobileNo(rs.getString(7));
				bean.setEmail(rs.getString(8));
				bean.setCreatedBy(rs.getString(9));
				bean.setModifiedBy(rs.getString(10));
				bean.setCreateDateTime(rs.getTimestamp(11));
				bean.setModifiedDatetime(rs.getTimestamp(12));

			}
			rs.close();
		} catch (Exception e) {
			log.error("Database Exception..", e);
			// throw new ApplicationException("Exception : Exception in getting User by
			// Email");
		} finally {
			JDBCDataSource.closeConnection(conn);
		}
		log.debug("Model findBy Email End");
		return bean;
	}
    /**
	 * find record by pk from database
	 * @param pk
	 * @return bean
	 * @throws ApplicationException
	 */

	public StudentBean findByPK(long pk) throws ApplicationException {
		log.debug("Model findByPK Started");
		StringBuffer sql = new StringBuffer("SELECT * FROM ST_STUDENT WHERE ID=?");
		StudentBean bean = null;
		Connection conn = null;
		try {
			conn = JDBCDataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			pstmt.setLong(1, pk);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				bean = new StudentBean();
				bean.setId(rs.getLong(1));
				bean.setCollegeId(rs.getLong(2));
				bean.setCollegeName(rs.getString(3));
				bean.setFirstName(rs.getString(4));
				bean.setLastName(rs.getString(5));
				bean.setDob(rs.getDate(6));
				bean.setMobileNo(rs.getString(7));
				bean.setEmail(rs.getString(8));
				bean.setCreatedBy(rs.getString(9));
				bean.setModifiedBy(rs.getString(10));
				bean.setCreateDateTime(rs.getTimestamp(11));
				bean.setModifiedDatetime(rs.getTimestamp(12));
			}
			rs.close();
		} catch (Exception e) {
			log.error("Database Exception..", e);
			throw new ApplicationException("Exception : Exception in getting User by pk");
		} finally {
			JDBCDataSource.closeConnection(conn);
		}
		log.debug("Model findByPK End");
		return bean;
	}
	/**
	 * delete record in database
	 * @param bean
	 * @throws ApplicationException
	 */
	public void delete(StudentBean bean) throws ApplicationException {
		log.debug("Delete Modal start");
		Connection conn = null;
		try {
			conn = JDBCDataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement("DELETE FROM ST_STUDENT WHERE ID=?");
			pstmt.setLong(1, bean.getId());
			pstmt.executeUpdate();
			conn.commit();
			pstmt.close();
		}catch (Exception e) {
			log.error("Database Exception.." + e);
			try {
				conn.rollback();
			} catch (Exception ex) {
				throw new ApplicationException("Exception : delete rollback exception  " + ex.getMessage());
			}
			throw new ApplicationException("Exception : Exception in delete Student");
		} finally {
			JDBCDataSource.closeConnection(conn);
		}
		log.debug("Model delete End");
	}
	/**
	 * update record in database
	 * @param bean
	 * @throws ApplicationException
	 * @throws DublicateRecordException 
	 * @throws DuplicateRecordException
	 */
   public void update(StudentBean bean) throws ApplicationException, DublicateRecordException {
	   log.debug("Update Modal start!!!!");
	   System.out.println(bean.getId());
	   Connection conn = null;
	   StudentBean beanExist = findByEmailId(bean.getEmail()); 
	   if(beanExist!=null && beanExist.getId()!=bean.getId()) {
		   throw new DublicateRecordException("Email Id is already exists!!!");
	   }
	   System.out.println("ID1"+bean.getId());
	   CollegeModal cModal = new CollegeModal();
	   System.out.println(bean.getCollegeId());
	   try {
	   CollegeBean collegebean = cModal.findByPK(bean.getCollegeId());
	   bean.setCollegeName(collegebean.getName());
	   System.out.println(bean.getCollegeName());
	   }catch(Exception e) {
		   e.printStackTrace();
	   }
	   
	   try {
		   conn = JDBCDataSource.getConnection();
		   conn.setAutoCommit(false);
		   System.out.println(bean.getId());
		   PreparedStatement pstmt = conn.prepareStatement(
					"UPDATE ST_STUDENT SET COLLEGE_ID=?,COLLEGE_NAME=?,FIRST_NAME=?,LAST_NAME=?,DATE_OF_BIRTH=?,MOBILE_NO=?,EMAIL=?,CREATED_BY=?,MODIFIED_BY=?,CREATED_DATETIME=?,MODIFIED_DATETIME=? WHERE ID=?");

			pstmt.setLong(1, bean.getCollegeId());
			pstmt.setString(2, bean.getCollegeName());
			pstmt.setString(3, bean.getFirstName());
			pstmt.setString(4, bean.getLastName());
			pstmt.setDate(5, new java.sql.Date(bean.getDob().getTime()));
			pstmt.setString(6, bean.getMobileNo());
			pstmt.setString(7, bean.getEmail());
			pstmt.setString(8, bean.getCreatedBy());
			pstmt.setString(9, bean.getModifiedBy());
			pstmt.setTimestamp(10, bean.getCreateDateTime());
			pstmt.setTimestamp(11, bean.getModifiedDatetime());
			pstmt.setLong(12, bean.getId());
        
			pstmt.executeUpdate();
			conn.commit();
			pstmt.close();
	   }catch (Exception e) {
			log.error("Database Exception....", e);
			try {
				conn.rollback();
			} catch (Exception ex) {
				 throw new ApplicationException("Exception : Delete rollback");
			}
			// throw new ApplicationException("Exception in updating Student");
		} finally {
			JDBCDataSource.closeConnection(conn);
		}
		log.debug("Model update End");
	}
   
   /**
	 * search record from database
	 * @param bean
	 * @return list
	 * @throws ApplicationException
	 */

	public List search(StudentBean bean) throws ApplicationException {
		return search(bean, 0, 0);
	}
	
	/**
	 * search record from database
	 * @param bean
	 * @param pageNo
	 * @param PageSize
	 * @return list
	 * @throws ApplicationException
	 */
    public List search(StudentBean bean , int pageNo , int pageSize) throws ApplicationException {
    	log.debug("Search model start");
    	StringBuffer sql = new StringBuffer("SELECT * FROM ST_STUDENT WHERE 1=1");
    	
    	if(bean!=null) {
    		if (bean.getId() > 0) {
				sql.append(" AND id = " + bean.getId());
			}
			if (bean.getFirstName() != null && bean.getFirstName().length() > 0) {
				sql.append(" AND FIRST_NAME like '" + bean.getFirstName() + "%'");
			}
			if (bean.getLastName() != null && bean.getLastName().length() > 0) {
				sql.append(" AND LAST_NAME like '" + bean.getLastName() + "%'");
			}
			if (bean.getDob() != null && bean.getDob().getDate() > 0) {
				sql.append(" AND DOB = " + bean.getDob());
			}
			if (bean.getMobileNo() != null && bean.getMobileNo().length() > 0) {
				sql.append(" AND MOBILE_NO like '" + bean.getMobileNo() + "%'");
			}
			if (bean.getEmail() != null && bean.getEmail().length() > 0) {
				sql.append(" AND EMAIL like '" + bean.getEmail() + "%'");
			}
			if (bean.getCollegeName() != null && bean.getCollegeName().length() > 0) {
				sql.append(" AND COLLEGE_NAME = '" + bean.getCollegeName()+"%'");
			}
			if (bean.getCollegeName() != null && bean.getCollegeName().length() > 0) {
				sql.append(" AND COLLEGE_NAME = '" + bean.getCollegeName()+"%'");
			}
			if (bean.getCollegeId() > 0) {
				sql.append(" AND COLLEGE_ID = '" + bean.getCollegeId()+"%'");
			}
			if(pageSize>0) {
				pageNo = (pageNo-1)*pageSize;
				sql.append(" Limit " + pageNo + ", " + pageSize);
			}
    	}
			
			ArrayList  list = new ArrayList();
			Connection conn = null;
			try {
				conn = JDBCDataSource.getConnection();
			    PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			    ResultSet rs = pstmt.executeQuery();
			    while(rs.next()) {
			    	bean = new StudentBean();
			    	bean.setId(rs.getLong(1));
					bean.setCollegeId(rs.getLong(2));
					bean.setCollegeName(rs.getString(3));
					bean.setFirstName(rs.getString(4));
					bean.setLastName(rs.getString(5));
					bean.setDob(rs.getDate(6));
					bean.setMobileNo(rs.getString(7));
					bean.setEmail(rs.getString(8));
					bean.setCreatedBy(rs.getString(9));
					bean.setModifiedBy(rs.getString(10));
					bean.setCreateDateTime(rs.getTimestamp(11));
					bean.setModifiedDatetime(rs.getTimestamp(12));
					list.add(bean);
			    }
			    rs.close();
			}catch (Exception e) {
				log.error("Database Exception..", e);
				throw new ApplicationException("Exception : Exception in search Student");
			} finally {
				JDBCDataSource.closeConnection(conn);
			}
			log.debug("Model search End");
			return list;
    	}   
    /**
	 * list of records from database
	 * @return list
	 * @throws ApplicationException
	 */
	
	public List list() throws ApplicationException {
		return list(0, 0);
	}
	
	/**
	 * list of records from database
	 * @param pageNo
	 * @param pageSize
	 * @return list
	 * @throws ApplicationException
	 */
	public List list(int pageNo, int pageSize) throws ApplicationException {
		log.debug("Model list Started");
		ArrayList list = new ArrayList();
		StringBuffer sql = new StringBuffer("select * from ST_STUDENT");

		if (pageSize > 0) {
			pageNo = (pageNo - 1) * pageSize;
			sql.append(" limit " + pageNo + "," + pageSize);

		}

		Connection conn = null;

		try {
			conn = JDBCDataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				StudentBean bean = new StudentBean();
				bean.setId(rs.getLong(1));
				bean.setCollegeId(rs.getLong(2));
				bean.setCollegeName(rs.getString(3));
				bean.setFirstName(rs.getString(4));
				bean.setLastName(rs.getString(5));
				bean.setDob(rs.getDate(6));
				bean.setMobileNo(rs.getString(7));
				bean.setEmail(rs.getString(8));
				bean.setCreatedBy(rs.getString(9));
				bean.setModifiedBy(rs.getString(10));
				bean.setCreateDateTime(rs.getTimestamp(11));
				bean.setModifiedDatetime(rs.getTimestamp(12));
				list.add(bean);
			}
			rs.close();
		} catch (Exception e) {
			log.error("Database Exception...", e);
			throw new ApplicationException("Exception : Exception in getting list of Student");
		} finally {
			JDBCDataSource.closeConnection(conn);
		}
		log.debug("Model list End");
		return list;
	}
	

}

