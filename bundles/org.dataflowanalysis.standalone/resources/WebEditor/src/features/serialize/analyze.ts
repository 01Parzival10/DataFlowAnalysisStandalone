import { inject, injectable, optional } from "inversify";
import { Command, CommandExecutionContext, LocalModelSource, SModelRootImpl, TYPES } from "sprotty";
import { Action, SModelRoot } from "sprotty-protocol";
import { LabelType, LabelTypeRegistry } from "../labels/labelTypeRegistry";
import { DynamicChildrenProcessor } from "../dfdElements/dynamicChildren";
import { EditorMode, EditorModeController } from "../editorMode/editorModeController";
import { ws, setModelSource } from '../../index';

/**
 * Type that contains all data related to a diagram.
 * This contains the sprotty diagram model and other data related to it.
 */
export interface SavedDiagram {
    model: SModelRoot;
    labelTypes?: LabelType[];
    editorMode?: EditorMode;
}

export interface AnalyzeDiagramAction extends Action {
    kind: typeof AnalyzeDiagramAction.KIND;
    suggestedFileName: string;
}
export namespace AnalyzeDiagramAction {
    export const KIND = "save-diagram";

    export function create(suggestedFileName?: string): AnalyzeDiagramAction {
        return {
            kind: KIND,
            suggestedFileName: suggestedFileName ?? "diagram.json",
        };
    }
}

@injectable()
export class AnalyzeDiagramCommand extends Command {
    static readonly KIND = AnalyzeDiagramAction.KIND;
    @inject(TYPES.ModelSource)
    private modelSource: LocalModelSource = new LocalModelSource();
    @inject(DynamicChildrenProcessor)
    private dynamicChildrenProcessor: DynamicChildrenProcessor = new DynamicChildrenProcessor();
    @inject(LabelTypeRegistry)
    @optional()
    private labelTypeRegistry?: LabelTypeRegistry;
    @inject(EditorModeController)
    @optional()
    private editorModeController?: EditorModeController;

    constructor(@inject(TYPES.Action) private action: AnalyzeDiagramAction) {
        super();
    }

    execute(context: CommandExecutionContext): SModelRootImpl {
        // Convert the model to JSON
        // Do a copy because we're going to modify it
        const modelCopy = JSON.parse(JSON.stringify(this.modelSource.model));
        // Remove element children that are implementation detail
        this.dynamicChildrenProcessor.processGraphChildren(modelCopy, "remove");

        // Export the diagram as a JSON data URL.
        const diagram: SavedDiagram = {
            model: modelCopy,
            labelTypes: this.labelTypeRegistry?.getLabelTypes(),
            editorMode: this.editorModeController?.getCurrentMode(),
        };
        const diagramJson = JSON.stringify(diagram, undefined, 4);
        console.log("Hi + " + diagramJson);

        ws.send(diagramJson);
        return context.root;
    }

    // Saving cannot be meaningfully undone/redone

    undo(context: CommandExecutionContext): SModelRootImpl {
        return context.root;
    }

    redo(context: CommandExecutionContext): SModelRootImpl {
        return context.root;
    }

}


