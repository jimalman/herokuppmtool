package com.jimalman.ppmtool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jimalman.ppmtool.domain.Backlog;
import com.jimalman.ppmtool.domain.Project;
import com.jimalman.ppmtool.domain.ProjectTask;
import com.jimalman.ppmtool.exceptions.ProjectNotFoundException;
import com.jimalman.ppmtool.repositories.BacklogRepository;
import com.jimalman.ppmtool.repositories.ProjectRepository;
import com.jimalman.ppmtool.repositories.ProjectTaskRepository;

@Service
public class ProjectTaskService {

	@Autowired
	private BacklogRepository backlogRepository;

	@Autowired
	private ProjectTaskRepository projectTaskRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired 
	private ProjectService projectService;

	public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username) {

		// PT to be added to a specific project, project != null, BL exists
		Backlog backlog = projectService.findProjectByIdentifier(projectIdentifier, username).getBacklog();//backlogRepository.findByProjectIdentifier(projectIdentifier);
		// Set the BL to the PT
		projectTask.setBacklog(backlog);
		// Want project sequence like PROJ-1 PROJ-2
		Integer backlogSequence = backlog.getPTSequence();
		// Update BL sequence
		backlogSequence++;
		backlog.setPTSequence(backlogSequence);

		// Add Sequence to Project Task
		projectTask.setProjectSequence(projectIdentifier + "-" + backlogSequence);
		projectTask.setProjectIdentifier(projectIdentifier);

		// Initial priority when priority is null
		if(projectTask.getPriority() == 0 || projectTask.getPriority() == null ) {
			projectTask.setPriority(3);
		}
		// Initial status when status is null
		if(projectTask.getStatus() == null || projectTask.getStatus() == "") {
			projectTask.setStatus("TO_DO");
		}

		return projectTaskRepository.save(projectTask);
	}

	public Iterable<ProjectTask>findBacklogById(String id, String username) {
		
		projectService.findProjectByIdentifier(id, username);

		Project project = projectRepository.findByProjectIdentifier(id);

		if(project==null) {
			throw new ProjectNotFoundException("Project ID '" + id.toUpperCase() + "' does not exist");
		}
		return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
	}

	public ProjectTask findPTByProjectSequence(String backlogId, String ptId, String username) {
		// make sure we are searching on an existing backlog
		projectService.findProjectByIdentifier(backlogId, username);
		// make sure our task exists
		ProjectTask projectTask = projectTaskRepository.findByProjectSequence(ptId);

		if(projectTask == null) {
			throw new ProjectNotFoundException("Project Task '" + ptId + "' does not exist");
		}

		// make sure the backlog/project id in the path is the right project
		if(!projectTask.getProjectIdentifier().equals(backlogId)) {
			throw new ProjectNotFoundException("Project Task '" + ptId + "' does not exist in project " + backlogId);
		}
		return projectTaskRepository.findByProjectSequence(ptId);
	}

	public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlogId, String ptId, String username) {
		// find existing project task
		ProjectTask projectTask = findPTByProjectSequence(backlogId, ptId, username);

		// replace it with updated task
		projectTask = updatedTask;

		// save update
		return projectTaskRepository.save(projectTask);
	}

	public void deletePTByProjectSequence(String backlogId, String ptId, String username) {
		// find existing project task
		ProjectTask projectTask = findPTByProjectSequence(backlogId, ptId, username);

		projectTaskRepository.delete(projectTask);
	}
}
