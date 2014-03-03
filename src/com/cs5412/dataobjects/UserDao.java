package com.cs5412.dataobjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

import com.mongodb.DBObject;

public class UserDao implements DBObject{

	public static final String FULLNAME="fullName";
	public static final String EMAIL="email";
	public static final String USERNAME="username";
	public static final String PASSWORD="password";
	
	private ObjectId _id;
	private String fullName;
	private String email;
	private String username;
	private String password;

	public UserDao() {}
	public UserDao( String fullName, String email, String username, String password )
    {
		this.fullName = fullName;
		this.email    =	 email;
		this.username = username;
		this.password = password;
    }
	public ObjectId getId() { return this._id; }
	public void setId( ObjectId _id ) { this._id = _id; }
	public void generateId() { if( this._id == null ) this._id = new ObjectId(); }
	
	public String getFullName() { return this.fullName; }
	public void setFullName( String fullName ) { this.fullName = fullName; }
	public String getUsername() { return this.username; }
	public void setUsername( String username ) { this.username = username; }
	
	public String getEmail() { return this.email; }
	public void setEmail( String email ) { this.email = email; }
	
	public String getPassword() { return this.password; }
	public void setPassword( String password ) { this.password = password; }
	
	@Override
	public boolean containsField(String field) {
	
		return( field.equals( "_id" )
	            || field.equals( FULLNAME )
	            || field.equals( EMAIL )
	            || field.equals( USERNAME )
	            || field.equals( PASSWORD ) );
	}

	@Override
	public boolean containsKey(String key) {
		// TODO Auto-generated method stub
		return containsField( key );
	}

	@Override
	public Object get(String field) {
		 
		if( field.equals( "_id" ) )    return this._id;
        if( field.equals( FULLNAME ) )   return this.fullName;
        if( field.equals( EMAIL ) ) return this.email;
        if( field.equals( USERNAME ) )   return this.username;
        if( field.equals( PASSWORD ) )  return this.password;
        return null;
	}

	@Override
	public Set<String> keySet() {
		Set< String > set = new HashSet< String >();
        set.add( "_id" );
        set.add( FULLNAME );
        set.add( EMAIL );
        set.add( USERNAME );
        set.add( PASSWORD );

        return set;
	}

	@Override
	public Object put(String field, Object object) {
		if( field.equals( "_id" ) )
        {
            this._id = ( ObjectId ) object;
            return object;
        }
        if( field.equals( FULLNAME ) )
        {
            this.fullName = ( String ) object;
            return object;
        }
        if( field.equals( EMAIL ) )
        {
            this.email = ( String ) object;
            return object;
        }
        if( field.equals( USERNAME ) )
        {
            this.username = ( String ) object;
            return object;
        }
        if( field.equals( PASSWORD ) )
        {
            this.password = ( String ) object;
            return object;
        }
        return null;
	}

	@Override
	public void putAll(BSONObject bson) {
		for( String key : bson.keySet() )
            put( key, bson.get( key ) );
		
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void putAll(@SuppressWarnings( "rawtypes" ) Map map) {
		for( Map.Entry< String, Object > entry : ( Set< Map.Entry< String, Object > > ) map.entrySet() )
            put( entry.getKey().toString(), entry.getValue() );
		
	}

	@Override
	public Object removeField(String arg0) {
		throw new RuntimeException("Unsupported method.");
	}

	@SuppressWarnings( "rawtypes" )
	@Override
	public Map toMap() {

        Map< String, Object > map = new HashMap< String, Object >();

        if( this._id != null )    map.put( "_id",    this._id );
        if( this.fullName != null )      map.put( FULLNAME,   this.fullName );
        if( this.email != null ) map.put( EMAIL, this.email );
        if( this.username != null )   map.put( USERNAME,   this.username );
        if( this.password != null )  map.put( PASSWORD,  this.password );

        return map;
	}

	@Override
	public boolean isPartialObject() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void markAsPartialObject() {
		throw new RuntimeException("Method not implemented.");
		
	}
	  

        

 
}
