package com.microservices.margo.workflow_service.api.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/workflows/", "/workflows"})
@RequiredArgsConstructor
public class WorkflowController {
}
