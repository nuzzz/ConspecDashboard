package com.example.helper;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler{
	
//    @ExceptionHandler(MaxUploadSizeExceededException.class)
//    public ModelAndView handleMaxSizeFileException(MultipartException multiException, HttpEntity<String> req , ResponseEntity<String> response ) {
//    	ModelAndView modelAndView = new ModelAndView("file");
//        modelAndView.getModel().put("message", "File too large!");
//        return modelAndView;
//    }
	
    @ExceptionHandler(MultipartException.class)
    public String handleError1(MultipartException e, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", "Error: Uploaded file size exceeds limit (5MB)");
        return "redirect:/upload";

    }
}