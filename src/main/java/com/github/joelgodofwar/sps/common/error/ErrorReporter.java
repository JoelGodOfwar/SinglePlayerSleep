package com.github.joelgodofwar.sps.common.error;

import org.bukkit.plugin.Plugin;

import com.github.joelgodofwar.sps.common.error.Report.ReportBuilder;

/**
 * Represents an object that can forward an error {@link Report} to the display and permanent storage.
 * 
 * @author Kristian
 */
public interface ErrorReporter {
	/**
	 * Prints a small minimal error report regarding an exception from another plugin.
	 * @param sender - the other plugin.
	 * @param methodName - name of the caller method.
	 * @param error - the exception itself.
	 */
	void reportMinimal(Plugin sender, String methodName, Throwable error);

	/**
	 * Prints a small minimal error report regarding an exception from another plugin.
	 * @param sender - the other plugin.
	 * @param methodName - name of the caller method.
	 * @param error - the exception itself.
	 * @param parameters - any relevant parameters to print.
	 */
	void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters);

	/**
	 * Prints a debug message from the current sender.
	 * <p>
	 * Most users will not see this message.
	 * @param sender - the sender.
	 * @param report - the report.
	 */
	void reportDebug(Object sender, Report report);

	/**
	 * Prints a debug message from the current sender.
	 * @param sender - the sender.
	 * @param builder - the report builder.
	 */
	void reportDebug(Object sender, ReportBuilder builder);

	/**
	 * Prints a warning message from the current plugin.
	 * @param sender - the object containing the caller method.
	 * @param report - an error report to include.
	 */
	void reportWarning(Object sender, Report report);

	/**
	 * Prints a warning message from the current plugin.
	 * @param sender - the object containing the caller method.
	 * @param reportBuilder - an error report builder that will be used to get the report.
	 */
	void reportWarning(Object sender, ReportBuilder reportBuilder);

	/**
	 * Prints a detailed error report about an unhandled exception.
	 * @param sender - the object containing the caller method.
	 * @param report - an error report to include.
	 */
	void reportDetailed(Object sender, Report report);

	/**
	 * Prints a detailed error report about an unhandled exception.
	 * @param sender - the object containing the caller method.
	 * @param reportBuilder - an error report builder that will be used to get the report.
	 */
	void reportDetailed(Object sender, ReportBuilder reportBuilder);
}