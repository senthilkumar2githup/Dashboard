package org.hpccsystems.dashboard.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;


@Service("dashboardDao")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DashboardDaoImpl implements DashboardDao {
    
    private JdbcTemplate jdbcTemplate;
    
    private RowMapper<Dashboard> dahboardRowMapper = (rs, index) -> {
        Dashboard dashboard = new Dashboard();
        dashboard.setId(rs.getInt("id"));
        dashboard.setApplicationId(rs.getString("application_id"));
        dashboard.setHpccId(rs.getString("hpcc_id"));
        dashboard.setName(rs.getString("name"));
        dashboard.setVisiblity(rs.getInt("visibility"));
        dashboard.setCompositionName(rs.getString("composition_name"));
        dashboard.setLayout(rs.getString("layout"));
        return dashboard;
    };

    @Autowired
    public void setJdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void insertDashboard(Dashboard dashboard,String userId) {
       
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", dashboard.getName());
        parameters.put("user_id", userId);
        parameters.put("application_id", dashboard.getApplicationId());
        parameters.put("visibility", dashboard.getVisiblity());
        parameters.put("hpcc_id", dashboard.getHpccId());
        parameters.put("composition_name", dashboard.getCompositionName());

        Number dashboardId = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
                .withTableName("dashboard")
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(parameters);

        dashboard.setId(dashboardId.intValue());
    }

    @Override
    public List<Dashboard> getDashboards(String userId, String applicationId) {
        
        String sql="SELECT * FROM dashboard WHERE user_id = ? AND application_id = ?";
        return jdbcTemplate.query(sql, new Object[]{userId,applicationId}, dahboardRowMapper);
    }
    
    public void deleteDashboard(final Integer dashboardId) throws DataAccessException {
        jdbcTemplate.update("delete from dashboard where id=?", new Object[] { 
                 dashboardId
         });
     }
    
    @Override
    public void updateDashboard(final Dashboard dashboard) throws DataAccessException {
    	jdbcTemplate.update("update dashboard set name=?, visibility=?,last_updated_date=?, hpcc_id=?,composition_name=? where id=?", new Object[] { 
    			dashboard.getName(),    			
    			dashboard.getVisiblity(),
    			new java.sql.Date(new Date().getTime()),
    			dashboard.getHpccId(),
    			dashboard.getCompositionName(),
    			dashboard.getId()
            });
    	
    
    	
    }

    @Override
    public void updateLayout(Dashboard dashboard) {
        jdbcTemplate.update("update dashboard set layout=? where id=?", new Object[] {
           dashboard.getLayout(),
           dashboard.getId()
        });
        
    }
    
    @Override
    public String getWssqlport(String hpccId) {
        String sql="select wssqlport from hpccclusterdetails where hpcc_id=?";
        List<Map<String,Object>> result=jdbcTemplate.queryForList(sql, new Object[]{hpccId});
        String WssqlPort=null;
        if(!result.isEmpty()){
            WssqlPort=(String)result.iterator().next().get("wssqlport");
        }
        return WssqlPort;
    }
   
}



