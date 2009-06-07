package org.codehaus.mojo.exec;

/*
 * Copyright 2005-2006 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;

/**
 *
 */
public class ExecutableDependency
{
    private String groupId;

    private String artifactId;

    public ExecutableDependency()
    {
    }

    public String getGroupId()
    {
        return this.groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return this.artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public boolean matches( Artifact artifact )
    {
        return artifact.getGroupId().equals( this.getGroupId() )
            && artifact.getArtifactId().equals( this.getArtifactId() );
    }

    public String toString()
    {
        return this.groupId + ":" + this.artifactId;
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final ExecutableDependency that = (ExecutableDependency) o;

        if ( artifactId != null ? !artifactId.equals( that.artifactId ) : that.artifactId != null )
        {
            return false;
        }
        if ( groupId != null ? !groupId.equals( that.groupId ) : that.groupId != null )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = ( groupId != null ? groupId.hashCode() : 0 );
        result = 29 * result + ( artifactId != null ? artifactId.hashCode() : 0 );
        return result;
    }
}
