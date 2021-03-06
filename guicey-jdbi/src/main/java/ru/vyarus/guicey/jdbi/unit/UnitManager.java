package ru.vyarus.guicey.jdbi.unit;

import com.google.common.base.Preconditions;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Manages JDBI {@link Handle} for current unit of work. This handle must be used by all JDBI proxies.
 * Unit of work is thread-bound (all actions in one thread participate in one unit of work).
 * It is not intended to be used directly (only in really rare cases when manual unit required
 * without opening transaction).
 * <p>
 * Raw provider may be injected to obtain current handle: {@code @Inject Provider<Handle>}.
 * In all other cases transaction annotation must be used to wrap code into unit of work using guice aop.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.jdbi.tx.TransactionTemplate for manual transaction definition
 * @since 4.12.2016
 */
@Singleton
public class UnitManager implements Provider<Handle> {

    private final DBI dbi;
    private final ThreadLocal<Handle> unit = new ThreadLocal<>();

    @Inject
    public UnitManager(final DBI dbi) {
        this.dbi = dbi;
    }

    @Override
    public Handle get() {
        Preconditions.checkState(isUnitStarted(), "Unit of work not started yet");
        return unit.get();
    }

    /**
     * @return true if unit of work started (and handle could be obtained), false otherwise
     */
    public boolean isUnitStarted() {
        return unit.get() != null;
    }

    /**
     * Starts unit of work.
     *
     * @throws IllegalStateException if unit of work already started
     */
    public void beginUnit() {
        Preconditions.checkState(!isUnitStarted(), "Unit of work already started");
        final Handle handle = dbi.open();
        unit.set(handle);
    }

    /**
     * Finish unit of work. Note: does not commit transaction, but only close context handle.
     *
     * @throws IllegalStateException when no opened unit of work
     */
    public void endUnit() {
        Preconditions.checkState(isUnitStarted(), "Stop called outside of unit of work");
        unit.get().close();
        unit.remove();
    }
}
