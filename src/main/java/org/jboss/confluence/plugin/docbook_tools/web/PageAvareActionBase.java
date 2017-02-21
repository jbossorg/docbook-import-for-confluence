/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.confluence.plugin.docbook_tools.web;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.actions.PageAware;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.actions.SpaceAware;

/**
 * Action base for actions which need page selected.
 * 
 * @author Vlastimil Elias (velias at redhat dot com) (C) 2011 Red Hat Inc.
 */
public abstract class PageAvareActionBase extends ConfluenceActionSupport implements SpaceAware, PageAware {

	/**
	 * Permission constant - 'Space - Export' permission.
	 * 
	 * @see #hasPermissionForSpace(String)
	 */
	public static final String PERMISSION_EXPORTSPACE = SpacePermission.EXPORT_SPACE_PERMISSION;

	/**
	 * Permission constant - 'Pages - Add' permission.
	 * 
	 * @see #hasPermissionForSpace(String)
	 */
	public static final String PERMISSION_CREATEPAGE = SpacePermission.CREATEEDIT_PAGE_PERMISSION;

	private Space space;
	private Page page;

	public PageAvareActionBase() {
		super();
	}

	/**
	 * Check user permission for space.
	 * 
	 * @param permissionType identifier to check
	 * @return true if current user has given permission for current space
	 */
	protected boolean hasPermissionForSpace(String permissionType) {
		return spacePermissionManager.hasPermission(permissionType, space, getRemoteUser());
	}

	@Override
	public Page getPage() {
		return page;
	}

	@Override
	public boolean isLatestVersionRequired() {
		return true;
	}

	@Override
	public boolean isPageRequired() {
		return true;
	}

	@Override
	public boolean isViewPermissionRequired() {
		return true;
	}

	@Override
	public void setPage(AbstractPage page) {
		if (!(page instanceof Page)) {
			throw new IllegalArgumentException("We can only process Pages");
		} else {
			this.page = (Page) page;
			return;
		}
	}

	@Override
	public boolean isSpaceRequired() {
		return true;
	}

	@Override
	public Space getSpace() {
		return space;
	}

	@Override
	public void setSpace(Space space) {
		this.space = space;
	}

}